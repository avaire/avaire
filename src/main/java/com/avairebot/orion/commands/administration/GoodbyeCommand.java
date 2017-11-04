package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.ChannelTransformer;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.ComparatorUtil;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GoodbyeCommand extends Command {

    public GoodbyeCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Goodbye Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the goodbye messages on or off for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("goodbye", "bye");
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

        if (channelTransformer == null) {
            if (!guildTransformer.createChannelTransformer(message.getTextChannel())) {
                MessageFactory.makeError(message,
                    "Something went wrong while trying to create the channel transformer object, please contact one of my developers to look into this issue."
                ).queue();
                return false;
            }
            channelTransformer = guildTransformer.getChannel(message.getTextChannel().getId());
        }

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        switch (type) {
            case TRUE:
            case FALSE:
                channelTransformer.getGoodbye().setEnabled(type.getValue());
                break;

            case UNKNOWN:
                channelTransformer.getGoodbye().setEnabled(!channelTransformer.getGoodbye().isEnabled());
        }

        try {
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson()));

            String note = "";
            if (channelTransformer.getGoodbye().isEnabled()) {
                note = "\nYou can customize the message by using `.goodbyemessage [message]`";
            }

            MessageFactory.makeSuccess(message, "The `Goodbye` module has been **:status** for the :channel channel." + note)
                .set("status", channelTransformer.getGoodbye().isEnabled() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            orion.getLogger().fatal(ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
