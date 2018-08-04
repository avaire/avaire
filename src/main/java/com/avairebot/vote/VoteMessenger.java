package com.avairebot.vote;

import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.awt.*;

public class VoteMessenger {

    public MessageEmbed buildVoteWithPointsMessage(int points) {
        return buildVoteWithPointsMessage("Thanks for voting!", points);
    }

    public MessageEmbed buildVoteWithPointsMessage(String title, int points) {
        return MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#E91E63"))
            .setTitle(title, "https://discordbots.org/bot/avaire")
            .setFooter("Don't want to receive messages when you vote? use \"!voteopt out\"", null)
            .setDescription(String.format(
                "Thanks for voting for [AvaIre](https://discordbots.org/bot/avaire)! It's really appreciated ❤"
                    + "\nYou now have **%s** vote points, rewards for vote points is coming soon! <a:lurk:425394751357845506>"
                    + "\nYou now also have access to the `!volume` and `!default-volume` commands for the next 24 hours on servers you have permission to run them on.", points))
            .build();
    }

    public void sendVoteWithPointsMessageInDM(@Nonnull User user, int points) {
        user.openPrivateChannel().queue(message -> message.sendMessage(
            buildVoteWithPointsMessage(points)
        ).queue(null, RestActionUtil.ignore), RestActionUtil.ignore);
    }

    public void sendMustVoteMessage(MessageChannel channel) {
        sendMustVoteMessage(channel, null);
    }

    public void sendMustVoteMessage(MessageChannel channel, String feature) {
        channel.sendMessage(MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#E91E63"))
            .setTitle("Vote for AvaIre on DBL", "https://discordbots.org/bot/avaire")
            .setDescription(String.format(
                "You must vote to %s, voting is free and only takes a few seconds\nTo get started, head over to:\n\nhttps://discordbots.org/bot/avaire\n\nOnce you've voted you'll gain access to this, and other commands for the next 24 hours!",
                feature == null ? "use this command" : feature
            ))
            .build()
        ).queue();
    }
}
