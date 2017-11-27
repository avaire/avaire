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
import java.util.Collections;
import java.util.List;

public class AiCommand extends Command {

    public AiCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "AI Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the AI(Artificial Intelligence) on/off for the current channel.";
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
        return Collections.singletonList("ai");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,2,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message.getGuild());
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

        channelTransformer.getAI().setEnabled(!channelTransformer.getAI().isEnabled());

        try {
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson()));

            MessageFactory.makeSuccess(message, "The `Artificial Intelligence` module has been **:status** for the :channel channel.")
                .set("status", channelTransformer.getAI().isEnabled() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            Orion.getLogger().error(ex.getMessage(), ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
