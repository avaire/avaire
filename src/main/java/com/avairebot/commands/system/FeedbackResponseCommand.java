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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.database.collection.DataRow;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeedbackResponseCommand extends SystemCommand {

    public FeedbackResponseCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Feedback Response Command";
    }

    @Override
    public String getDescription() {
        return "Responses to a feedback message with the given ID, the channel the original feedback message was sent in will be used for the feedback, along with the message +  the response and author information.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <id> <message>` - Responds to the given feedback ID with the message.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 23 Thanks for the feedback <3`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("feedback");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM_ROLE;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "id");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        DataRow feedback = getFeedbackObject(NumberUtil.parseInt(args[0], -1));
        if (feedback == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "id", "feedback id");
        }

        TextChannel channel = avaire.getShardManager().getTextChannelById(feedback.getString("channel_id"));
        if (channel == null) {
            return sendErrorMessage(context, "The original text channel for the given feedback ID no longer exists, can't send feedback message.");
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            .replaceAll("\\\\n", "\n");

        PlaceholderMessage response = context.makeEmbeddedMessage()
            .setDescription(message)
            .setTitle("Feedback Response by " + buildAuthorString(context.getAuthor()))
            .addField("Original Feedback", feedback.getString("message"), false);

        User originalAuthor = avaire.getShardManager().getUserById(feedback.getString("user_id"));
        if (originalAuthor != null) {
            response.addField("Original Feedback By", buildAuthorString(originalAuthor), false);
        }

        channel.sendMessage(response.buildEmbed()).queue(responseMessage -> {
            context.makeSuccess("Successfully sent response to the feedback with an ID of #:id :emote")
                .set("id", feedback.getInt("id"))
                .set("emote", "<:tickYes:319985232306765825>")
                .queue();

            try {
                avaire.getDatabase().newQueryBuilder(Constants.FEEDBACK_TABLE_NAME)
                    .where("id", feedback.getInt("id"))
                    .update(statement -> {
                        statement.set("response", message, true);
                        statement.set("response_id", responseMessage.getId());
                    });
            } catch (SQLException ignored) {
                //
            }
        }, error -> sendErrorMessage(context, "Failed to send feedback response: {0}", error.getMessage()));

        return false;
    }

    private String buildAuthorString(User user) {
        return String.format("%s#%s (ID: %s)", user.getName(), user.getDiscriminator(), user.getId());
    }

    private DataRow getFeedbackObject(int id) {
        if (id < 1) {
            return null;
        }

        try {
            return avaire.getDatabase().newQueryBuilder(Constants.FEEDBACK_TABLE_NAME)
                .where("id", id)
                .get().first();
        } catch (SQLException ignored) {
            return null;
        }
    }
}
