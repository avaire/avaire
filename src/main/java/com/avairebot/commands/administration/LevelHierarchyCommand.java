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

public class LevelHierarchyCommand extends Command {

    public LevelHierarchyCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Level Hierarchy Command";
    }

    @Override
    public String getDescription() {
        return "Level Hierarchy determines if level roles give to users should be removed once they level up and get the next role, or if they should keep all of their roles, when the level hierarchy is enabled and a user levels up to get the next role, all other level roles they have will be removed, if they level up to a level without a level role, nothing will happen to them, the feature can be toggled on and off using this command.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current level hierarchy status.",
            "`:command <on/off>` - Toggles the feature on or off."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command on` - Toggles the feature on.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddLevelRoleCommand.class,
            RemoveLevelRoleCommand.class,
            ListLevelRolesCommand.class,
            LevelModifierCommand.class,
            LevelAlertsCommand.class,
            ChannelLevelCommand.class,
            LevelCommand.class,
            AdministrateExperienceCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("levelhierarchy", "hierarchy");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:guild,1,5",
            "requireOne:user,general.manage_roles,general.manage_server"
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

        if (args.length == 0) {
            return sendHierarchyStatus(context, guildTransformer);
        }

        ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[0]);
        if (type.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
            return sendErrorMessage(context, context.i18n("invalidTypeGiven"));
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getIdLong())
                .update(statement -> statement.set("hierarchy", type.getValue() ? "1" : "0"));

            guildTransformer.setLevelHierarchy(type.getValue());

            context.makeSuccess(context.i18n("message"))
                .set("status", type.getValue()
                    ? context.i18n("status.enabled")
                    : context.i18n("status.disabled")
                ).queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error("Error while saving the hierarchy status: " + e.getMessage(), e);

            return sendErrorMessage(context, "Error while saving the hierarchy status: " + e.getMessage());
        }
    }

    private boolean sendHierarchyStatus(CommandMessage context, GuildTransformer transformer) {
        context.makeInfo(context.i18n("statusMessage"))
            .set("command", generateCommandTrigger(context.getMessage()) + " <on/off>")
            .set("status", transformer.isLevelHierarchy()
                ? context.i18n("status.enabled")
                : context.i18n("status.disabled")
            ).queue();

        return true;
    }
}
