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

package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.contracts.middleware.ThrottleMessage;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class ThrottleMiddleware extends Middleware {

    public static final Cache<String, ThrottleEntity> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    public ThrottleMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        return String.format("**This command can only be used `%s` time(s) every `%s` seconds per %s**",
            arguments[1], arguments[2], arguments[0].equalsIgnoreCase("guild") ? "server" : arguments[0]
        );
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (args.length < 3) {
            AvaIre.getLogger().warn(String.format(
                "\"%s\" is parsing invalid amount of arguments to the throttle middleware, 3 arguments are required.", stack.getCommand()
            ));
            return stack.next();
        }

        ThrottleType type = ThrottleType.fromName(args[0]);

        try {
            int maxAttempts = NumberUtil.parseInt(args[1], 2);
            int decaySeconds = NumberUtil.parseInt(args[2], 5);

            String fingerprint = type.generateCacheString(message, stack);

            ThrottleEntity entity = getEntityFromCache(fingerprint, maxAttempts, decaySeconds);
            if (entity.getHits() >= maxAttempts) {
                Carbon expires = type.equals(ThrottleType.USER)
                    ? avaire.getBlacklist().getRatelimit().hit(type, message.getAuthor().getIdLong())
                    : avaire.getBlacklist().getRatelimit().hit(type, message.getGuild().getIdLong());

                if (expires != null) {
                    avaire.getBlacklist().getRatelimit().sendBlacklistMessage(
                        type.equals(ThrottleType.USER) ? message.getAuthor() : message.getChannel(), expires
                    );
                    return false;
                }

                return cancelCommandThrottleRequest(message, stack, entity);
            }

            boolean response = stack.next();

            if (response) {
                entity.incrementHit();
            }

            return response;
        } catch (NumberFormatException e) {
            AvaIre.getLogger().warn(String.format(
                "Invalid integers given to throttle command by \"%s\", args: (%s, %s)", stack.getCommand().getName(), args[1], args[2]
            ));
        }
        return false;
    }

    private boolean cancelCommandThrottleRequest(Message message, MiddlewareStack stack, ThrottleEntity entity) {
        Metrics.commandsRatelimited.labels(stack.getCommand().getClass().getSimpleName()).inc();

        return runMessageCheck(message, () -> {
            String throttleMessage = "Too many `:command` attempts. Please try again in **:time** seconds.";

            ThrottleMessage annotation = stack.getCommand().getClass().getAnnotation(ThrottleMessage.class);
            if (annotation != null && annotation.message().trim().length() > 0) {
                if (annotation.overwrite()) {
                    throttleMessage = annotation.message();
                } else {
                    throttleMessage += annotation.message();
                }
            }

            MessageFactory.makeWarning(message, throttleMessage)
                .set("command", stack.getCommand().getName())
                .set("time", ((entity.getTime() - System.currentTimeMillis()) / 1000) + 1)
                .set("prefix", stack.getCommand().generateCommandPrefix(message))
                .queue(newMessage -> newMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

            return false;
        });
    }

    private ThrottleEntity getEntityFromCache(String fingerprint, int maxAttempts, int decaySeconds) {
        ThrottleEntity entity = (ThrottleEntity) CacheUtil.getUncheckedUnwrapped(cache, fingerprint,
            () -> new ThrottleEntity(maxAttempts, decaySeconds)
        );

        if (entity.hasExpired()) {
            cache.invalidate(fingerprint);
            return getEntityFromCache(fingerprint, maxAttempts, decaySeconds);
        }

        return entity;
    }

    public enum ThrottleType {

        USER("user", "throttle.user.%s.%s.%s"),
        CHANNEL("channel", "throttle.channel.%s.%s.%s"),
        GUILD("guild", "throttle.guild.%s.%s");

        private final String name;
        private final String cache;

        ThrottleType(String name, String cache) {
            this.name = name;
            this.cache = cache;
        }

        public static ThrottleType fromName(String name) {
            for (ThrottleType type : values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return ThrottleType.USER;
        }

        public String getName() {
            return name;
        }

        public String generateCacheString(Message message, MiddlewareStack stack) {
            if (!this.equals(ThrottleType.USER) && message.getGuild() == null) {
                return USER.generateCacheString(message, stack);
            }

            String cacheFingerprint = generateCacheFingerprint(stack);

            switch (this) {
                case USER:
                    return String.format(cache,
                        message.getGuild() == null ? "private" : message.getGuild().getId(),
                        message.getAuthor().getId(),
                        cacheFingerprint);

                case CHANNEL:
                    return String.format(cache,
                        message.getGuild().getId(),
                        message.getChannel().getId(),
                        cacheFingerprint);

                case GUILD:
                    return String.format(cache,
                        message.getGuild().getId(),
                        cacheFingerprint);

                default:
                    return ThrottleType.USER.generateCacheString(message, stack);
            }
        }

        private String generateCacheFingerprint(MiddlewareStack stack) {
            CacheFingerprint annotation = stack.getCommand().getClass().getAnnotation(CacheFingerprint.class);

            if (annotation == null || annotation.name().length() == 0) {
                return stack.getCommand().getName();
            }

            return annotation.name();
        }
    }

    private static class ThrottleEntity {

        private final int maxAttempts;
        private final long time;
        private int hit;

        ThrottleEntity(int maxAttempts, int decaySeconds) {
            this.time = System.currentTimeMillis() + (decaySeconds * 1000);
            this.maxAttempts = maxAttempts;
            this.hit = 0;
        }

        public int getHits() {
            return hit;
        }

        public void incrementHit() {
            hit++;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public long getTime() {
            return time;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() > time;
        }
    }
}
