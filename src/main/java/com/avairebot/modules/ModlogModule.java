package com.avairebot.modules;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;

public class ModlogModule {

    public static void log(AvaIre avaire, CommandMessage context, ModlogAction action) {
        log(avaire, context.getGuild(), action);
    }

    public static void log(AvaIre avaire, Message message, ModlogAction action) {
        log(avaire, message.getGuild(), action);
    }

    public static void log(AvaIre avaire, Guild guild, ModlogAction action) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        if (transformer != null) {
            log(avaire, guild, transformer, action);
        }
    }

    private static void log(AvaIre avaire, Guild guild, GuildTransformer transformer, ModlogAction action) {
        if (transformer.getModlog() == null) {
            return;
        }

        TextChannel channel = guild.getTextChannelById(transformer.getModlog());
        if (channel == null) {
            return;
        }

        transformer.setModlogCase(transformer.getModlogCase() + 1);

        EmbedBuilder builder = MessageFactory.createEmbeddedBuilder()
            .setTitle(action.getType().getName() + " | Case #" + transformer.getModlogCase())
            .setColor(action.getType().getColor())
            .setTimestamp(Instant.now());

        switch (action.getType()) {
            case KICK:
            case BAN:
            case SOFT_BAN:
                builder
                    .addField("User", action.getStringifiedTarget(), true)
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Reason", formatReason(transformer, action.getMessage()), false);
                break;

            case PURGE:
                builder
                    .addField("Moderator", action.getStringifiedModerator(), false)
                    .addField("Action", action.getMessage(), false);
                break;

            case VOICE_KICK:
                String[] split = action.getMessage().split("\n");
                builder
                    .addField("User", action.getStringifiedTarget(), true)
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Voice Channel", split[0], false)
                    .addField("Reason", formatReason(transformer, split[1]), false);
                break;
        }

        channel.sendMessage(builder.build()).queue(success -> {
            try {
                avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", guild.getId())
                    .update(statement -> {
                        statement.set("modlog_case", transformer.getModlogCase());
                    });

                if (!action.getType().equals(ModlogType.PURGE)) {
                    logActionToTheDatabase(avaire, guild, action, success, transformer.getModlogCase());
                }
            } catch (SQLException ignored) {
                //
            }
        }, RestActionUtil.IGNORE);
    }

    private static void logActionToTheDatabase(AvaIre avaire, Guild guild, ModlogAction action, Message message, int modlogCase) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
                .insert(statement -> {
                    statement.set("modlogCase", modlogCase);
                    statement.set("type", action.getType().getId());
                    statement.set("guild_id", guild.getId());
                    statement.set("user_id", action.getModerator().getId());

                    if (action.getTarget() != null) {
                        statement.set("target_id", action.getTarget().getId());
                    }

                    if (message != null) {
                        statement.set("message_id", message.getId());
                    }

                    if (action.getType().equals(ModlogType.VOICE_KICK)) {
                        statement.set("reason", formatReason(null, action.getMessage().split("\n")[1]));
                    } else {
                        statement.set("reason", formatReason(null, action.getMessage()));
                    }
                });
        } catch (SQLException ignored) {
            //
        }
    }

    private static String formatReason(@Nullable GuildTransformer transformer, String reason) {
        if (reason == null || reason.trim().equalsIgnoreCase("No reason was given.")) {
            if (transformer != null) {
                // TODO: Replace the "!" prefix trigger with the correct prefix trigger per-guild for the reason command.
                return String.format(
                    "Moderator do `!reason %s <reason>`",
                    transformer.getModlogCase()
                );
            }
            return null;
        }
        return reason;
    }

    public enum ModlogType {

        KICK(1, "Kick", MessageType.WARNING.getColor()),
        VOICE_KICK(2, "Voice Kick", MessageType.WARNING.getColor()),
        SOFT_BAN(3, "Soft Ban", MessageType.ERROR.getColor()),
        BAN(4, "Ban", MessageType.ERROR.getColor()),
        PURGE(5, "Purge", MessageType.INFO.getColor()),
        WARN(6, "Warning", MessageType.WARNING.getColor());

        final int id;
        final String name;
        final Color color;

        ModlogType(int id, String name, Color color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }

    public static class ModlogAction {

        private final User moderator;
        private final User target;

        private ModlogType type;
        private String message = null;

        public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target) {
            this(action, moderator, target, null);
        }

        public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target, String message) {
            this.moderator = moderator;
            this.target = target;
            this.type = action;
            this.message = message;
        }

        public User getModerator() {
            return moderator;
        }

        public String getStringifiedModerator() {
            return stringifyUser(moderator);
        }

        public User getTarget() {
            return target;
        }

        public String getStringifiedTarget() {
            return stringifyUser(target);
        }

        public ModlogType getType() {
            return type;
        }

        public void setType(ModlogType type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String stringifyUser(User user) {
            if (user == null) {
                return "";
            }
            return user.getName() + "#" + user.getDiscriminator() + " (" + user.getAsMention() + ")";
        }
    }
}
