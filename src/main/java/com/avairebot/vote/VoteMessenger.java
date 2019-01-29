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

package com.avairebot.vote;

import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class VoteMessenger {

    /**
     * Builds the "Thanks for Voting" message with the
     * given amount of vote points for the user.
     *
     * @param points The amounts of points that should be displayed in the message.
     * @return The JDA message embed object with the thanks for voting message.
     */
    public MessageEmbed buildThanksForVotingMessage(int points) {
        return buildThanksForVotingMessage("Thanks for voting!", points);
    }

    /**
     * Builds the "Thanks for Voting" message with the given title,
     * and the given amount of vote points for the user.
     *
     * @param title  The title that should be used for the message.
     * @param points The amounts of points that should be displayed in the message.
     * @return The JDA message embed object with the thanks for voting message.
     */
    public MessageEmbed buildThanksForVotingMessage(@Nonnull String title, int points) {
        return MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#E91E63"))
            .setTitle(title, "https://discordbots.org/bot/avaire")
            .setFooter("Don't want to receive messages when you vote? use \"!voteopt out\"", null)
            .setDescription(String.format(
                "Thanks for voting for [AvaIre](https://discordbots.org/bot/avaire)! It's really appreciated ❤"
                    + "\nYou now have **%s** vote points, rewards for vote points is coming soon! <a:lurk:425394751357845506>"
                    + "\nYou now also have access to the `!volume` and `!default-volume` commands for the next 12 hours on servers you have permission to run them on, you can also save up your vote points to buy special rank backgrounds using the `!backgrounds` command.", points))
            .build();
    }

    /**
     * Sends the "Thanks for Voting" message in a direct message
     * to the given user with the given amount of points.
     *
     * @param user   The user that should received the message.
     * @param points The amounts of points that should be displayed in the message.
     */
    public void SendThanksForVotingMessageInDM(@Nonnull User user, int points) {
        user.openPrivateChannel().queue(message -> message.sendMessage(
            buildThanksForVotingMessage(points)
        ).queue(null, RestActionUtil.ignore), RestActionUtil.ignore);
    }

    /**
     * Sends the "Must Vote" message to the given channel, letting the
     * user know that they must vote to use the current command.
     *
     * @param channel The channel the message should be sent to.
     */
    public void sendMustVoteMessage(@Nonnull MessageChannel channel) {
        sendMustVoteMessage(channel, null);
    }

    /**
     * Sends the "Must Vote" message to the given channel, letting the
     * user know that they must vote to use the given feature.
     *
     * @param channel The channel the message should be sent to.
     * @param feature The feature that the user has to vote for to use, or
     *                <code>null</code> to specify the current command.
     */
    public void sendMustVoteMessage(@Nonnull MessageChannel channel, @Nullable String feature) {
        channel.sendMessage(MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#E91E63"))
            .setTitle("Vote for AvaIre on DBL", "https://discordbots.org/bot/avaire")
            .setDescription(String.format(
                "You must vote to %s, voting is free and only takes a few seconds\nTo get started, head over to:\n\nhttps://discordbots.org/bot/avaire\n\nOnce you've voted you'll gain access to this, and other commands for the next 12 hours!",
                feature == null ? "use this command" : feature
            ))
            .build()
        ).queue();
    }
}
