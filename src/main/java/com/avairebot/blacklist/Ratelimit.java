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
import com.avairebot.time.Carbon;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * His the blacklisting ratelimit, if the given user ID has reached the
     * maximum number of hits within the allowed timeframe, the user will
     * be auto blacklisted for a certain amount of time, the time the
     * user is blacklisted for depends on how many earlier offense
     * they have, all the punishment levels can be seen in
     * the {@link #levels punishments array}.
     *
     * @param userId The ID of the user that should hit the ratelimit.
     * @return <code>True</code> if the user was blacklisted, <code>False</code> otherwise.
     */
    public boolean hit(long userId) {
        Rate rate = CacheUtil.getUncheckedUnwrapped(cache, userId);
        if (rate == null) {
            // This should never happen, if it does we'll just return
            // true to not block any commands by a valid user.
            return false;
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (rate) {
            rate.hit();

            if (rate.getHits() < hitLimit) {
                return false;
            }
        }

        Long last = rate.getLast();
        if (last != null && last < System.currentTimeMillis() - 2500) {
            return true;
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

        return true;
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
