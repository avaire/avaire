package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.ChannelTransformer;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GoodbyeMessageCommand extends Command {

    public GoodbyeMessageCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Goodbye Message Command";
    }

    @Override
    public String getDescription() {
        return "Sets the message that should be sent when a user leaves the server, this command can only be used if the goodbye module is enabled for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Resets the goodbye back to the default message.",
            "`:command <message>` - Sets the goodbye message to the given message."
        );
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("goodbyemessage", "byemsg");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }


    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message);
        ChannelTransformer channelTransformer = guildTransformer.getChannel(message.getTextChannel().getId());

        if (channelTransformer == null || !channelTransformer.getGoodbye().isEnabled()) {
            return sendErrorMessage(message, "The `goodbye` module must be enabled to use this command, you can enable the `goodbye` module by using the `.goodbye` command.");
        }

        channelTransformer.getGoodbye().setMessage(args.length == 0 ? null : String.join(" ", args));

        try {
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson()));

            MessageFactory.makeSuccess(message, "The `Goodbye` module message has been set :message")
                .set("message", channelTransformer.getGoodbye().getMessage() != null ?
                    "to:\n\n`" + channelTransformer.getGoodbye().getMessage() + "`" :
                    "back to the default."
                ).queue();
        } catch (SQLException ex) {
            Orion.getLogger().error("Failed to update the goodbye message", ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: :error")
                .set("error", ex.getMessage())
                .queue();
            return false;
        }
        return true;
    }
}
