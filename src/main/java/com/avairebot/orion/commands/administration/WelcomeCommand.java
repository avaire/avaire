package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.CacheFingerprint;
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

@CacheFingerprint(name = "welcome-goodbye-command")
public class WelcomeCommand extends Command {

    public WelcomeCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Welcome Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the welcome messages on or off for the current channel.";
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
        return Arrays.asList("welcome", "wel");
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
            return sendErrorMessage(message,
                "Something went wrong while trying to get the channel transformer object, please contact one of my developers to look into this issue."
            );
        }

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        switch (type) {
            case TRUE:
            case FALSE:
                channelTransformer.getWelcome().setEnabled(type.getValue());
                break;

            case UNKNOWN:
                channelTransformer.getWelcome().setEnabled(!channelTransformer.getWelcome().isEnabled());
        }

        try {
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            String note = "";
            if (channelTransformer.getWelcome().isEnabled()) {
                note = "\nYou can customize the message by using `.welcomemessage [message]`";
            }

            MessageFactory.makeSuccess(message, "The `Welcome` module has been **:status** for the :channel channel.:note")
                .set("status", channelTransformer.getWelcome().isEnabled() ? "Enabled" : "Disabled")
                .set("note", note)
                .queue();
        } catch (SQLException ex) {
            Orion.getLogger().error(ex.getMessage(), ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
