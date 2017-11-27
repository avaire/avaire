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

public class WelcomeMessageCommand extends Command {

    public WelcomeMessageCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Welcome Message Command";
    }

    @Override
    public String getDescription() {
        return "Sets the message that should be sent when a user joins the server, this command can only be used if the welcome module is enabled for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Resets the welcome back to the default message.",
            "`:command <message>` - Sets the welcome message to the given message."
        );
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("welcomemessage", "welmsg");
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

        if (channelTransformer == null || !channelTransformer.getWelcome().isEnabled()) {
            return sendErrorMessage(message, "The `welcome` module must be enabled to use this command, you can enable the `welcome` module by using the `.welcome` command.");
        }

        channelTransformer.getWelcome().setMessage(args.length == 0 ? null : String.join(" ", args));

        try {
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson()));

            MessageFactory.makeSuccess(message, "The `Welcome` module message has been set :note")
                .set("note", channelTransformer.getWelcome().getMessage() != null ?
                    "to:\n\n`" + channelTransformer.getWelcome().getMessage() + "`" :
                    "back to the default."
                ).queue();
        } catch (SQLException ex) {
            Orion.getLogger().error(ex.getMessage(), ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }
        return true;
    }
}
