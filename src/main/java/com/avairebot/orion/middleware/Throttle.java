package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheItem;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class Throttle extends AbstractMiddleware {

    public Throttle(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (args.length < 3) {
            orion.logger.warning("\"%s\" is parsing invalid amount of arguments to the throttle middleware, 3 arguments are required.", stack.getCommand());
            return stack.next();
        }

        ThrottleType type = ThrottleType.fromName(args[0]);

        try {
            int maxAttempts = Integer.parseInt(args[1]);
            int decaySeconds = Integer.parseInt(args[2]);

            String fingerprint = type.generateCacheString(message, stack);

            CacheItem item = orion.cache.getRaw(fingerprint);
            if (item == null) {
                item = new CacheItem(fingerprint, 0, -1);
            }

            int attempts = (Integer) item.getValue();
            if (attempts >= maxAttempts) {
                MessageFactory.makeWarning(
                        message,
                        "Too many `%s` attempts. Please try again in **%s** seconds.",
                        stack.getCommand().getName(),
                        ((item.getTime() - System.currentTimeMillis()) / 1000) + 1
                ).queue();
                return false;
            }

            if (stack.next()) {
                orion.cache.put(fingerprint, ++attempts, decaySeconds);
            }

        } catch (NumberFormatException e) {
            orion.logger.warning("Invalid integers given to throttle command by \"%s\", args: (%s, %s)", stack.getCommand().getName(), args[1], args[2]);
        }
        return false;
    }

    private enum ThrottleType {
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
            switch (this) {
                case USER:
                    return String.format(cache,
                            message.getGuild().getId(),
                            message.getAuthor().getId(),
                            stack.getCommand().getName());
                case CHANNEL:
                    return String.format(cache,
                            message.getGuild().getId(),
                            message.getChannel().getId(),
                            stack.getCommand().getName());
                case GUILD:
                    return String.format(cache,
                            message.getGuild().getId(),
                            stack.getCommand().getName());
                default:
                    return ThrottleType.USER.generateCacheString(message, stack);
            }
        }
    }
}
