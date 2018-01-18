package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shared.DiscordConstants;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class FeedbackCommand extends Command {

    public FeedbackCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Feedback Command";
    }

    @Override
    public String getDescription() {
        return "Gives feedback about AvaIre to the developers and staff team";
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
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `message`, you must include a message.");
        }

        TextChannel feedbackChannel = avaire.getTextChannelById(DiscordConstants.FEEDBACK_CHANNEL_ID);
        if (feedbackChannel == null) {
            return sendErrorMessage(message, "Invalid feedback channel defined, the text channel could not be found!");
        }

        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(feedbackChannel)
            .addField("Feedback", String.join(" ", args), false)
            .addField("Channel", buildChannel(message.getChannel()), false)
            .setFooter("Author ID: " + message.getAuthor().getId())
            .setTimestamp(Instant.now());

        placeholderMessage.setAuthor(
            message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator(),
            null,
            message.getAuthor().getEffectiveAvatarUrl()
        );

        if (message.getChannelType().isGuild() && message.getGuild() != null) {
            placeholderMessage.addField("Server", buildServer(message.getGuild()), false);
        }

        placeholderMessage.queue(newMessage -> {
            MessageFactory.makeSuccess(message, "Successfully sent feedback <:tickYes:319985232306765825>").queue();
        }, err -> {
            AvaIre.getLogger().error("Failed to send feedback message: " + err.getMessage(), err);
            MessageFactory.makeError(message, "Failed to send feedback message: " + err.getMessage()).queue();
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
