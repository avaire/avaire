package com.avairebot.modules;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
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
                    .addField("Reason", action.getMessage(), false);
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
                    .addField("Reason", split[1], false);
                break;
        }

        channel.sendMessage(builder.build()).queue(success -> {
            try {
                avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", guild.getId())
                    .update(statement -> {
                        statement.set("modlog_case", transformer.getModlogCase());
                    });
            } catch (SQLException ignored) {
                //
            }
        });
    }

    public enum ModlogType {

        KICK("Kick", MessageType.WARNING.getColor()),
        VOICE_KICK("Voice Kick", MessageType.WARNING.getColor()),
        SOFT_BAN("Soft Ban", MessageType.ERROR.getColor()),
        BAN("Ban", MessageType.ERROR.getColor()),
        PURGE("Purge", MessageType.INFO.getColor());

        final String name;
        final Color color;

        ModlogType(String name, Color color) {
            this.name = name;
            this.color = color;
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
