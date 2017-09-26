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
import java.util.Collections;
import java.util.List;

public class LevelCommand extends AbstractCommand {

    public LevelCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Level Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the Leveling system on or off for the current server";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Toggles the level feature on/off");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("level", "lvl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
                "require:general.manage_server",
                "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message);

        guildTransformer.setLevels(!guildTransformer.isLevels());

        try {
            orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .andWhere("id", message.getGuild().getId())
                    .update(statement -> statement.set("levels", guildTransformer.isLevels()));

            String note = "";
            if (guildTransformer.isLevels()) {
                note = String.format("\nLevel alerts are current `%s`, you can toggle them on or off with `.levelalerts`",
                        guildTransformer.isLevelAlerts() ? "Enabled" : "Disabled"
                );
            }

            MessageFactory.makeSuccess(message, "`Levels & Experience` has been `%s` for the server.%s",
                    guildTransformer.isLevels() ? "Enabled" : "Disabled",
                    note
            ).queue();
        } catch (SQLException ex) {
            orion.logger.fatal(ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: %s", ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
