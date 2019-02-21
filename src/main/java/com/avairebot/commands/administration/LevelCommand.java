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
            ChannelLevelCommand.class,
            LevelHierarchyCommand.class,
            LevelModifierCommand.class,
            LevelAlertsCommand.class,
            RankCommand.class,
            AdministrateExperienceCommand.class
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

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
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
                note = context.i18n("note",
                    context.i18n("status." + (guildTransformer.isLevelAlerts() ? "enabled" : "disabled")),
                    generateCommandPrefix(context.getMessage())
                );
            }

            context.makeSuccess(context.i18n("message"))
                .set("status", context.i18n("status." + (guildTransformer.isLevels() ? "enabled" : "disabled")))
                .set("note", note)
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
