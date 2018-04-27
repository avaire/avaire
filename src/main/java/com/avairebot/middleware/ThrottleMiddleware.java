package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheItem;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.contracts.middleware.ThrottleMessage;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

public class ThrottleMiddleware extends Middleware {

    public ThrottleMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(String[] arguments) {
        return String.format("**This command can only be used `%s` time(s) every `%s` seconds per %s**",
            arguments[1], arguments[2], arguments[0].equalsIgnoreCase("guild") ? "server" : arguments[0]
        );
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
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

            CacheItem item = avaire.getCache().getRaw(fingerprint);
            if (item == null) {
                item = new CacheItem(fingerprint, 0, -1);
            }

            int attempts = (Integer) item.getValue();
            if (attempts >= maxAttempts) {
                return cancelCommandThrottleRequest(message, stack, item);
            }

            if (stack.next()) {
                avaire.getCache().put(fingerprint, ++attempts, decaySeconds);
            }

        } catch (NumberFormatException e) {
            AvaIre.getLogger().warn(String.format(
                "Invalid integers given to throttle command by \"%s\", args: (%s, %s)", stack.getCommand().getName(), args[1], args[2]
            ));
        }
        return false;
    }

    private boolean cancelCommandThrottleRequest(Message message, MiddlewareStack stack, CacheItem item) {
        Metrics.commandsRatelimited.labels(stack.getCommand().getClass().getSimpleName()).inc();

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
            .set("time", ((item.getTime() - System.currentTimeMillis()) / 1000) + 1)
            .set("prefix", stack.getCommand().generateCommandPrefix(message))
            .queue();

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
}
