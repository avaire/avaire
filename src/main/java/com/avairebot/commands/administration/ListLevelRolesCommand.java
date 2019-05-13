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
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListLevelRolesCommand extends Command {

    public ListLevelRolesCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "List Level Roles Command";
    }

    @Override
    public String getDescription() {
        return "List all the leveling roles and the level require to get them.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Lists all the roles you can get for leveling up."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            RemoveLevelRoleCommand.class,
            LevelHierarchyCommand.class,
            AddLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("llr");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList(
            "throttle:channel,1,5"
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

        if (transformer.getLevelRoles().isEmpty()) {
            context.makeInfo(context.i18n("noLevelRoles"))
                .set("command", CommandHandler.getCommand(AddLevelRoleCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage()))
                .queue();

            return false;
        }

        ArrayList<Integer> keys = new ArrayList<>(
            transformer.getLevelRoles().keySet()
        );

        Collections.sort(keys);

        SimplePaginator<Integer> paginator = new SimplePaginator<>(keys, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            //noinspection SuspiciousMethodCalls
            Role role = context.getGuild().getRoleById(
                transformer.getLevelRoles().get(val)
            );

            if (role != null) {
                messages.add(
                    String.format(
                        "`%s` => `%s`",
                        val, role.getName()
                    )
                );
            }
        });

        messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())));

        context.makeSuccess(String.join("\n", messages))
            .setTitle(context.i18n("listRoles", paginator.getTotal()))
            .requestedBy(context.getMember())
            .queue();

        return false;
    }
}
