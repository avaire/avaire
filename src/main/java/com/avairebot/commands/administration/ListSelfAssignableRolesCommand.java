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

public class ListSelfAssignableRolesCommand extends Command {

    public ListSelfAssignableRolesCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "List Self Assignable Roles Command";
    }

    @Override
    public String getDescription() {
        return "List all the self-assignable roles, 10 per-page.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - List all the self-assignable roles for the server.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddSelfAssignableRoleCommand.class,
            RemoveSelfAssignableRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lsar");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.ROLE_ASSIGNMENTS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getSelfAssignableRoles().isEmpty()) {
            context.makeWarning(context.i18n("noSelfAssignableRoles")).queue();
            return true;
        }

        ArrayList<String> items = new ArrayList<>();
        for (String roleId : transformer.getSelfAssignableRoles().keySet()) {
            Role roleById = context.getGuild().getRoleById(roleId);
            if (roleById != null) {
                items.add(roleById.getName());
            }
        }

        Collections.sort(items);
        SimplePaginator<String> paginator = new SimplePaginator<>(items, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(String.format("**%s**", val)));
        messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())));

        context.makeSuccess(String.join("\n", messages))
            .setTitle(context.i18n("title", paginator.getTotal()))
            .requestedBy(context.getMember())
            .queue();

        return true;
    }
}
