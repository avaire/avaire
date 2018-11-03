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
import com.avairebot.language.I18n;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;

public class Modlog {

    /**
     * Logs an action to the modlog channel for the given context.
     *
     * @param avaire  The main AvaIre application instance.
     * @param context The command context the modlog action is occurring in.
     * @param action  The action that should be logged to the modlog.
     * @return Possibly-null, the case ID if the modlog was logged successfully,
     * otherwise <code>null</code> will be returned.
     */
    @Nullable
    public static String log(AvaIre avaire, CommandMessage context, ModlogAction action) {
        return log(avaire, context.getGuild(), action);
    }

    /**
     * Logs an action to the modlog channel for the given message.
     *
     * @param avaire  The main AvaIre application instance.
     * @param message The message that triggered the modlog action.
     * @param action  The action that should be logged to the modlog.
     * @return Possibly-null, the case ID if the modlog was logged successfully,
     * otherwise <code>null</code> will be returned.
     */
    @Nullable
    public static String log(AvaIre avaire, Message message, ModlogAction action) {
        return log(avaire, message.getGuild(), action);
    }

    /**
     * Logs an action to the modlog channel for the given guild.
     *
     * @param avaire The main AvaIre application instance.
     * @param guild  The guild the modlog action should be logged in.
     * @param action The action that should be logged to the modlog.
     * @return Possibly-null, the case ID if the modlog was logged successfully,
     * otherwise <code>null</code> will be returned.
     */
    @Nullable
    public static String log(AvaIre avaire, Guild guild, ModlogAction action) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        if (transformer != null) {
            return log(avaire, guild, transformer, action);
        }
        return null;
    }

    /**
     * Logs an action to the modlog channel for the given guild
     * using the guild transformer, and the modlog action.
     *
     * @param avaire      The main AvaIre application instance.
     * @param guild       The guild the modlog action should be logged in.
     * @param transformer The guild transformer containing all the guild settings used in the modlog action.
     * @param action      The action that should be logged to the modlog.
     * @return Possibly-null, the case ID if the modlog was logged successfully,
     * otherwise <code>null</code> will be returned.
     */
    @Nullable
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

        String[] split = null;
        EmbedBuilder builder = MessageFactory.createEmbeddedBuilder()
            .setTitle(I18n.format("{0} {1} | Case #{2}",
                action.getType().getEmote(),
                action.getType().getName(),
                transformer.getModlogCase()
            ))
            .setColor(action.getType().getColor())
            .setTimestamp(Instant.now());

        switch (action.getType()) {
            case WARN:
            case KICK:
            case BAN:
            case SOFT_BAN:
            case UNBAN:
                builder
                    .addField("User", action.getStringifiedTarget(), true)
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Reason", formatReason(transformer, action.getMessage()), false);
                break;

            case PURGE:
                builder
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Action", action.getMessage(), true)
                    .addField("Reason", formatReason(transformer, null), false);
                action.setMessage(null);
                break;

            case VOICE_KICK:
                //noinspection ConstantConditions
                split = action.getMessage().split("\n");
                builder
                    .addField("User", action.getStringifiedTarget(), true)
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Voice Channel", split[0], false)
                    .addField("Reason", formatReason(transformer, String.join("\n",
                        Arrays.copyOfRange(split, 1, split.length)
                    )), false);

                action.setMessage(String.join("\n",
                    Arrays.copyOfRange(split, 1, split.length)
                ));
                break;

            case PARDON:
                //noinspection ConstantConditions
                split = action.getMessage().split("\n");
                String[] modlogParts = split[0].split(":");
                builder
                    .addField("Pardoned Case ID", I18n.format("#[{0}](https://discordapp.com/channels/{1}/{2}/{3})",
                        modlogParts[0], transformer.getId(), transformer.getModlog(), modlogParts[1]
                    ), true)
                    .addField("Moderator", action.getStringifiedModerator(), true)
                    .addField("Reason", formatReason(transformer, String.join("\n",
                        Arrays.copyOfRange(split, 1, split.length)
                    )), false);

                action.setMessage(String.join("\n",
                    Arrays.copyOfRange(split, 1, split.length)
                ));
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

                logActionToTheDatabase(avaire, guild, action, success, transformer.getModlogCase());
            } catch (SQLException ignored) {
                //
            }
        }, RestActionUtil.ignore);

        return "" + transformer.getModlogCase();
    }

    /**
     * Notifies the given user about a modlog action by DMing them
     * a message, if the user have DM messages disabled, nothing
     * will be sent and the method will fail silently.
     *
     * @param user   The user that should be notified about a modlog action.
     * @param guild  The guild that the modlog action happened in.
     * @param action The modlog action that the user should be notified of.
     * @param caseId The case ID that is attached to the modlog action.
     */
    public static void notifyUser(User user, Guild guild, ModlogAction action, @Nullable String caseId) {
        String type = action.getType().getNotifyName();
        if (type == null || user.isBot()) {
            return;
        }

        user.openPrivateChannel().queue(channel -> {
            EmbedBuilder message = MessageFactory.createEmbeddedBuilder()
                .setColor(action.getType().getColor())
                .setDescription(String.format("%s You have been **%s** %s " + guild.getName(),
                    action.getType().getEmote(),
                    type,
                    action.getType().equals(ModlogType.WARN)
                        ? "in" : "from"
                ))
                .addField("Moderator", action.getModerator().getName() + "#" + action.getModerator().getDiscriminator(), true)
                .addField("Reason", action.getMessage(), true)
                .setTimestamp(Instant.now());

            if (caseId != null) {
                message.setFooter("Case ID #" + caseId, null);
            }

            channel.sendMessage(message.build()).queue(null, RestActionUtil.ignore);
        }, RestActionUtil.ignore);
    }

    private static void logActionToTheDatabase(AvaIre avaire, Guild guild, ModlogAction action, Message message, int modlogCase) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
                .useAsync(true)
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

                    statement.set("reason", formatReason(null, action.getMessage()), true);
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
