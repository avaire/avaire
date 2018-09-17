/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.blacklist;

import com.avairebot.contracts.blacklist.PunishmentLevel;
import com.avairebot.factories.MessageFactory;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.RestActionUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Ratelimit {

    /**
     * This represents that amount of times the rate limit can
     * be hit within the {@link #hitTime ratelimit timeframe}
     * before the user is auto blacklisted.
     */
    static final int hitLimit = 10;

    /**
     * This represents the amount of time in milliseconds that each
     * hit should be valid for, if all hit slots for a given user
     * is taken up, but one of the times exceeds the hit time
     * limit, the rate limit won't count it as exceeding
     * the rate limit.
     */
    static final long hitTime = 25 * 1000;

    /**
     * The cache loader for holding all the ratelimiter rates.
     */
    public static final LoadingCache<Long, Rate> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(hitTime, TimeUnit.MILLISECONDS)
        .build(CacheLoader.from(Rate::new));

    /**
     * The slf4j logger instance.
     */
    private static final Logger log = LoggerFactory.getLogger(Ratelimit.class);

    /**
     * The punishment level holder, this map holds all the users and their current
     * punishment level, with each offence, the punishment level(value) will go
     * up, increasing the time the user get auto-blacklisted for.
     */
    private static final Map<Long, Integer> punishments = new HashMap<>();

    /**
     * The punishment levels, each index of the levels list should be an
     * increasingly harsher punishment for repeating offenders.
     */
    private static final List<PunishmentLevel> levels = Arrays.asList(
        () -> Carbon.now().addMinute(),
        () -> Carbon.now().addMinutes(15),
        () -> Carbon.now().addMinutes(30),
        () -> Carbon.now().addHour(),
        () -> Carbon.now().addHours(6),
        () -> Carbon.now().addHours(12),
        () -> Carbon.now().addDay(),
        () -> Carbon.now().addDays(3),
        () -> Carbon.now().addDays(7)
    );

    /**
     * The blacklist instance that the ratelimit was created for.
     */
    private final Blacklist blacklist;

    /**
     * Creates a new ratelimit instance for the given blacklist.
     *
     * @param blacklist The blacklist instance to create the ratelimit for.
     */
    Ratelimit(Blacklist blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * Sends the blacklist message to the given use in a direct
     * message to let the user know that they have been added
     * to the blacklist automatically.
     *
     * @param user    The user that was blacklisted.
     * @param expires The carbon time instance for when the blacklist expires.
     */
    public void sendBlacklistMessage(User user, Carbon expires) {
        user.openPrivateChannel().queue(channel -> {
            channel.sendMessage(MessageFactory.createEmbeddedBuilder()
                .setColor(Color.decode("#A5306B"))
                .setTitle("Whoa there!", "https://avairebot.com/")
                .setFooter("Expires", null)
                .setTimestamp(expires.getTime().toInstant())
                .setDescription("Looks like you're using commands a bit too fast, I've banned you "
                    + "from using any commands, or earning any XP until you cool down a bit.\n"
                    + "Your ban expires in " + expires.addSecond().diffForHumans(true) + ", "
                    + "keep in mind repeating the behavior will get you banned for longer "
                    + "periods of time, eventually if you keep it up you will be banned "
                    + "from using any of my commands permanently."
                ).build()
            ).queue();
        }, RestActionUtil.ignore);
    }

    /**
     * His the blacklisting ratelimit, if the given user ID has reached the
     * maximum number of hits within the allowed timeframe, the user will
     * be auto blacklisted for a certain amount of time, the time the
     * user is blacklisted for depends on how many earlier offense
     * they have, all the punishment levels can be seen in
     * the {@link #levels punishments array}.
     *
     * @param userId The ID of the user that should hit the ratelimit.
     * @return Possibly-null, the time object matching when the blacklist expires,
     * or <code>null</code> if the user was not blacklisted.
     */
    @Nullable
    public Carbon hit(long userId) {
        Rate rate = CacheUtil.getUncheckedUnwrapped(cache, userId);
        if (rate == null) {
            // This should never happen, if it does we'll just return
            // null to not block any commands by a valid user.
            return null;
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (rate) {
            rate.hit();

            if (rate.getHits() < hitLimit) {
                return null;
            }
        }

        Long last = rate.getLast();

        // Checks if the user was blacklisted within the last two and half seconds,
        // the command handling process uses its own thread pool, because of that
        // it's possible to have two commands come in from the same user in a
        // very quick succession, instead of punishing the user twice, we
        // just cancel the blacklist hit here instead.
        if (last != null && last < System.currentTimeMillis() - 2500) {
            return null;
        }

        Carbon punishment = getPunishment(userId);

        log.info("{} has been added to the blacklist for excessive command usage, the blacklist expires {}.",
            userId, punishment.toDayDateTimeString()
        );

        blacklist.addIdToBlacklist(
            Scope.USER, userId,
            "Automatic blacklist due to excessive command usage.",
            punishment
        );

        return punishment;
    }

    /**
     * Gets the punishment for the given user ID, as well as
     * increasing their punishment level in the process.
     *
     * @param userId The ID of the user that the punishment should be fetched for.
     * @return The Carbon instance with the punishment expire time.
     */
    private Carbon getPunishment(long userId) {
        int level = punishments.getOrDefault(userId, -1) + 1;

        punishments.put(userId, level);

        return getPunishment(level);
    }

    /**
     * Gets the {@link Carbon} punishment object, representing
     * the time that the punishment should expire.
     *
     * @param level The level to get the punishment for.
     * @return The Carbon instance with the punishment expire time.
     */
    private Carbon getPunishment(int level) {
        if (level < 0) {
            return levels.get(0).generateTime();
        }
        return levels.get(level >= levels.size() ? levels.size() - 1 : level).generateTime();
    }
}
