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
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddLevelRoleCommand extends Command {

    public AddLevelRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Add Level Role Command";
    }

    @Override
    public String getDescription() {
        return "Adds a role to the leveling up table, roles on the table will be given to users once they level up and meet the requirements for the role.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <level requirement> <role>` - Adds to role to users when they level up and meet the level requirement."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 5 Regular` - Adds the Regular role to the level up table, users who are level 5 and up will get the role when they level up."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            LevelHierarchyCommand.class,
            ListLevelRolesCommand.class,
            RemoveLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("alr");
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

        if (transformer.getLevelRoles().size() >= transformer.getType().getLimits().getLevelRoles()) {
            context.makeWarning(context.i18n("noSlotsLeft"))
                .queue();

            return false;
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "level requirement");
        }

        int level = NumberUtil.parseInt(args[0], 0);
        if (level < 1) {
            context.makeWarning(context.i18n("invalidLevel"))
                .queue();

            return false;
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "role");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(),
            String.join(" ", Arrays.copyOfRange(args, 1, args.length))
        );

        if (role == null) {
            context.makeWarning(context.i18nRaw("administration.common.invalidRole"))
                .set("role", String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
                .queue();
            return false;
        }

        if (transformer.getLevelRoles().containsValue(role.getId())) {
            context.makeWarning(context.i18n("alreadyLevelRole"))
                .set("role", role.getName())
                .queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        if (transformer.getLevelRoles().containsKey(level)) {
            Role roleById = context.getGuild().getRoleById(
                transformer.getLevelRoles().get(level)
            );

            if (roleById != null) {
                context.makeWarning(context.i18n("alreadyARoleAtLevel"))
                    .set("level", level)
                    .queue();

                return false;
            }
        }

        try {
            transformer.getLevelRoles().put(level, role.getId());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_roles", AvaIre.gson.toJson(transformer.getLevelRoles()), true);
                });

            context.makeSuccess(context.i18n("success"))
                .set("slots", transformer.getType().getLimits().getLevelRoles() - transformer.getLevelRoles().size())
                .set("role", role.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
