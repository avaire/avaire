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
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoveLevelRoleCommand extends Command {

    public RemoveLevelRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Level Roles Command";
    }

    @Override
    public String getDescription() {
        return "Remove a role from the leveling up role table.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role>` - Removes the role from the leveling up role table.",
            "`:command <level>` - Removes the role that is assigned to the given level from the role table."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command Member`",
            "`:command 10`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelHierarchyCommand.class,
            ListLevelRolesCommand.class,
            AddLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rlr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Arrays.asList(
            CommandGroups.LEVEL_AND_EXPERIENCE,
            CommandGroups.ROLE_ASSIGNMENTS
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null || !transformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "role");
        }

        Role role = getRoleFromContext(context, transformer, args);
        if (role == null) {
            context.makeWarning(NumberUtil.isNumeric(args[0])
                ? context.i18n("noRolesLinked")
                : context.i18n("noRolesCalled")
            ).set("role", String.join(" ", args)).queue();
            return false;
        }

        if (!RoleUtil.canUserInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        if (!transformer.getLevelRoles().containsValue(role.getId())) {
            context.makeWarning(context.i18n("notALevelRole"))
                .set("name", role.getName())
                .queue();

            return false;
        }

        int level = -1;
        for (Map.Entry<Integer, String> entry : transformer.getLevelRoles().entrySet()) {
            if (entry.getValue().equals(role.getId())) {
                level = entry.getKey();
            }
        }

        try {
            transformer.getLevelRoles().remove(level);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_roles", AvaIre.gson.toJson(transformer.getLevelRoles()), true);
                });

            context.makeSuccess(context.i18n("message"))
                .set("slots", transformer.getType().getLimits().getLevelRoles() - transformer.getLevelRoles().size())
                .set("role", role.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Role getRoleFromContext(CommandContext context, GuildTransformer transformer, String[] args) {
        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), String.join(" ", args));
        if (role != null) {
            return role;
        }

        int roleLevel = NumberUtil.parseInt(args[0], 0);
        if (transformer.getLevelRoles().containsKey(roleLevel)) {
            return context.getGuild().getRoleById(
                transformer.getLevelRoles().get(roleLevel)
            );
        }

        return null;
    }
}
