package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelAlertsCommand extends AbstractCommand {

    public LevelAlertsCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Level Alerts Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the Leveling alerts system on or off for the current server or channel."
                + "\nThis command requires the `Levels & Experience` feature to be enabled for the server!";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
                "`:command` - Toggles the level alerts feature on/off",
                "`:command <channel>` - Toggles the level alerts feature on for the given channel"
        );
    }

    @Override
    public String getExampleUsage() {
        return "`:command`\n`:command #general`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("levelalerts", "lvlalert");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message);
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(message, "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `.level`");
        }

        boolean status = !guildTransformer.isLevelAlerts();

        String channelId = null;
        if (!message.getMentionedChannels().isEmpty()) {
            channelId = message.getMentionedChannels().get(0).getId();
            status = true;
        }

        guildTransformer.setLevelAlerts(status);
        guildTransformer.setLevelChannel(channelId);

        Map<String, Object> items = new HashMap<>();
        items.put("level_alerts", guildTransformer.isLevelAlerts());
        items.put("level_channel", guildTransformer.getLevelChannel());

        try {
            orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", guildTransformer.getId())
                    .update(items);

            MessageFactory.makeSuccess(message, "`Levels up alerts` has been `%s` for the server.%s",
                    status ? "Enabled" : "Disabled",
                    channelId == null ? "" : String.format("\nAll level up messages will be logged into the <#%s> channel.", channelId)
            ).queue();

            return true;
        } catch (SQLException ex) {
            orion.logger.fatal(ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: %s", ex.getMessage()).queue();
            return false;
        }
    }
}
