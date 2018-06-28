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
import java.util.List;

public class LevelAlertsCommand extends Command {

    public LevelAlertsCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command`",
            "`:command #general`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelCommand.class,
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("levelalerts", "lvlalert");
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
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(context,
                "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `.{0}togglelevel`",
                generateCommandPrefix(context.getMessage())
            );
        }

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        boolean status = !guildTransformer.isLevelAlerts();
        switch (type) {
            case TRUE:
            case FALSE:
                status = type.getValue();
        }

        String channelId = null;
        if (!context.getMentionedChannels().isEmpty()) {
            channelId = context.getMentionedChannels().get(0).getId();
            status = true;
        }

        guildTransformer.setLevelAlerts(status);
        guildTransformer.setLevelChannel(channelId);

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", guildTransformer.getId())
                .update(statement -> statement
                    .set("level_alerts", guildTransformer.isLevelAlerts())
                    .set("level_channel", guildTransformer.getLevelChannel())
                );

            String note = channelId == null ? "" : String.format("\nAll level up messages will be logged into the <#%s> channel.", channelId);
            context.makeSuccess("`Levels up alerts` has been `:status` for the server." + note)
                .set("status", status ? "Enabled" : "Disabled")
                .queue();

            return true;
        } catch (SQLException ex) {
            AvaIre.getLogger().error("Failed to update the guilds level column", ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }
    }
}
