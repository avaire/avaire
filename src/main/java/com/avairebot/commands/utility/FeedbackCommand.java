package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shared.DiscordConstants;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
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
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        TextChannel feedbackChannel = avaire.getShardManager().getTextChannelById(DiscordConstants.FEEDBACK_CHANNEL_ID);
        if (feedbackChannel == null) {
            return sendErrorMessage(context, "Invalid feedback channel defined, the text channel could not be found!");
        }

        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(feedbackChannel)
            .addField("Feedback", String.join(" ", args), false)
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
                    statement.set("message", String.join(" ", args), true);
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
