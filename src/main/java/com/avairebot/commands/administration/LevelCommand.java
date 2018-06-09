package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;

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
        return "Toggle Level Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the Leveling system on or off for the current server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Toggles the level feature on/off");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelAlertsCommand.class,
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("togglelevel", "tlvl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

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
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("levels", guildTransformer.isLevels()));

            String note = "";
            if (guildTransformer.isLevels()) {
                note = String.format("\nLevel alerts are current `%s`, you can toggle them on or off with `%slevelalerts`",
                    guildTransformer.isLevelAlerts() ? "Enabled" : "Disabled",
                    generateCommandPrefix(context.getMessage())
                );
            }

            context.makeSuccess("`Levels & Experience` has been `:status` for the server." + note)
                .set("status", guildTransformer.isLevels() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
