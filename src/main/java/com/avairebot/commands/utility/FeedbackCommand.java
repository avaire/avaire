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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shared.DiscordConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeedbackCommand extends Command {

    public static final Cache<Long, String> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    public FeedbackCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Feedback Command";
    }

    @Override
    public String getDescription() {
        return "Send feedback about Ava back to the developers and the staff team, any message passed to the command will be sent in the [#feedback](https://discord.gg/gt2FWER) channel on the [AvaIre Central](https://discord.gg/gt2FWER) server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Sends feedback to the devs.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command The thing about the stuff is doing stuff that doesn't make sense for the thing.`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("feedback");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,60");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        TextChannel feedbackChannel = avaire.getShardManager().getTextChannelById(DiscordConstants.FEEDBACK_CHANNEL_ID);
        if (feedbackChannel == null) {
            return sendErrorMessage(context, context.i18n("invalidFeedbackChannel"));
        }

        if (args.length == 0) {
            String message = cache.getIfPresent(context.getAuthor().getIdLong());

            if (message == null) {
                return sendErrorMessage(context, "errors.missingArgument", "message");
            }
            return sendFeedback(context, feedbackChannel, message);
        }

        String message = String.join(" ", args);
        if (message.length() < 32) {
            return sendErrorMessage(context, context.i18n("mustBe32CharactersOrMore"));
        }

        context.makeInfo(context.i18n("confirmation"))
            .setTitle("Last minute check!")
            .set("feedback", message.replaceAll("```", "\\`\\`\\`"))
            .set("command", generateCommandTrigger(context.getMessage()))
            .set("server", feedbackChannel.getGuild().getName())
            .queue();

        cache.put(context.getAuthor().getIdLong(), message);

        return false;
    }

    private boolean sendFeedback(CommandMessage context, TextChannel feedbackChannel, String message) {
        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(feedbackChannel)
            .addField("Feedback", message, false)
            .addField("Channel", buildChannel(context.getChannel()), false)
            .setFooter("Author ID: " + context.getAuthor().getId())
            .setTimestamp(Instant.now());

        placeholderMessage.setAuthor(
            context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator(),
            null,
            context.getAuthor().getEffectiveAvatarUrl()
        );

        if (context.isGuildMessage() && context.getGuild() != null) {
            placeholderMessage.addField("Server", buildServer(context.getGuild()), false);
        }

        placeholderMessage.queue(newMessage -> {
            context.makeSuccess(context.i18n("success")).queue();

            try {
                Collection collection = avaire.getDatabase().newQueryBuilder(Constants.FEEDBACK_TABLE_NAME).insert(statement -> {
                    statement.set("user_id", context.getAuthor().getIdLong());
                    statement.set("channel_id", context.isGuildMessage() ? context.getMessageChannel().getIdLong() : null);
                    statement.set("message", message, true);
                });

                if (!collection.isEmpty()) {
                    String id = collection.first().getString("id");

                    newMessage.editMessage(placeholderMessage
                        .setFooter("Author ID: " + context.getAuthor().getId() + " | ID: #" + id)
                        .buildEmbed()
                    ).queue();
                }
            } catch (SQLException e) {
                AvaIre.getLogger().error("Failed to store feedback in the database: {}", e.getMessage(), e);
            }
        }, err -> {
            AvaIre.getLogger().error("Failed to send feedback message: " + err.getMessage(), err);
            context.makeError(context.i18n("failedToSend"))
                .set("error", err.getMessage()).queue();
        });

        return true;
    }

    private String buildChannel(MessageChannel message) {
        return message.getName() + " (ID: " + message.getId() + ")";
    }

    private String buildServer(Guild server) {
        return server.getName() + " (ID: " + server.getId() + ")";
    }
}
