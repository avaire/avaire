package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AiCommand extends Command {

    public AiCommand(AvaIre avaire) {
        super(avaire, false);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());

        if (channelTransformer == null) {
            if (!guildTransformer.createChannelTransformer(context.getChannel())) {
                context.makeError(
                    "Something went wrong while trying to create the channel transformer object, please contact one of my developers to look into this issue."
                ).queue();
                return false;
            }
            channelTransformer = guildTransformer.getChannel(context.getChannel().getId());
        }

        channelTransformer.getAI().setEnabled(!channelTransformer.getAI().isEnabled());

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            context.makeSuccess("The `Artificial Intelligence` module has been **:status** for the :channel channel.")
                .set("status", channelTransformer.getAI().isEnabled() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
