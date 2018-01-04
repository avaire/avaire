package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.ComparatorUtil;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelCommand extends Command {

    public LevelCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("level", "lvl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(avaire, message);

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        switch (type) {
            case TRUE:
            case FALSE:
                guildTransformer.setLevels(type.getValue());
                break;

            case UNKNOWN:
                guildTransformer.setLevels(!guildTransformer.isLevels());
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("levels", guildTransformer.isLevels()));

            String note = "";
            if (guildTransformer.isLevels()) {
                note = String.format("\nLevel alerts are current `%s`, you can toggle them on or off with `.levelalerts`",
                    guildTransformer.isLevelAlerts() ? "Enabled" : "Disabled"
                );
            }

            MessageFactory.makeSuccess(message, "`Levels & Experience` has been `:status` for the server." + note)
                .set("status", guildTransformer.isLevels() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
