package com.avairebot.modlog;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.ModlogReasonCommand;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.events.ModlogActionEvent;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.Instant;

public class ModlogModule {

    public static String log(AvaIre avaire, CommandMessage context, ModlogAction action) {
        return log(avaire, context.getGuild(), action);
    }

    public static String log(AvaIre avaire, Message message, ModlogAction action) {
        return log(avaire, message.getGuild(), action);
    }

    public static String log(AvaIre avaire, Guild guild, ModlogAction action) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        if (transformer != null) {
            return log(avaire, guild, transformer, action);
        }
        return null;
    }

    public static String log(AvaIre avaire, Guild guild, GuildTransformer transformer, ModlogAction action) {
        if (transformer.getModlog() == null) {
            return null;
        }

        TextChannel channel = guild.getTextChannelById(transformer.getModlog());
        if (channel == null) {
            return null;
        }

        if (!channel.canTalk()) {
            return null;
        }

        transformer.setModlogCase(transformer.getModlogCase() + 1);

        EmbedBuilder builder = MessageFactory.createEmbeddedBuilder()
            .setTitle(action.getType().getName() + " | Case #" + transformer.getModlogCase())
            .setColor(action.getType().getColor())
            .setTimestamp(Instant.now());

        switch (action.getType()) {
            case WARN:
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

        avaire.getEventEmitter().push(new ModlogActionEvent(
            guild.getJDA(), action, transformer.getModlogCase()
        ));

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
        }, RestActionUtil.ignore);

        return "" + transformer.getModlogCase();
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

    @SuppressWarnings("ConstantConditions")
    private static String formatReason(@Nullable GuildTransformer transformer, String reason) {
        if (reason == null || reason.trim().equalsIgnoreCase("No reason was given.")) {
            if (transformer != null) {
                CommandContainer command = CommandHandler.getCommand(ModlogReasonCommand.class);
                String prefix = transformer.getPrefixes().getOrDefault(
                    command.getCategory().getName(), command.getDefaultPrefix()
                );

                return String.format(
                    "Moderator do `%sreason %s <reason>`",
                    prefix, transformer.getModlogCase()
                );
            }
            return null;
        }
        return reason;
    }
}
