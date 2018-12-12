/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
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
            LevelHierarchyCommand.class,
            LevelModifierCommand.class,
            ChannelLevelCommand.class,
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

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(context, "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
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

            context.makeSuccess(context.i18n("message"))
                .set("status", context.i18n("status." + (status ? "enabled" : "disabled")))
                .set("note", channelId == null ? "" : context.i18n("note", channelId))
                .queue();

            return true;
        } catch (SQLException ex) {
            AvaIre.getLogger().error("Failed to update the guilds level column", ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }
    }
}
