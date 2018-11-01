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
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoAssignRoleCommand extends Command {

    public AutoAssignRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Autorole Command";
    }

    @Override
    public String getDescription() {
        return "Automatically assigns a specified role to every user who joins the server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current auto assignable role if one is set.",
            "`:command <role>` - The role that should be auto assignable.",
            "`:command disable` - Disables the auto assignable role."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Member`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("autorole", "aar");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:bot,general.manage_roles",
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.ROLE_ASSIGNMENTS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();

        if (args.length == 0) {
            sendCurrentAutoRole(context, transformer).queue();
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            return disableAutoRole(context, transformer);
        }

        String roleName = String.join(" ", args);
        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), roleName);
        if (role == null) {
            context.makeWarning(context.i18nRaw("administration.common.invalidRole"))
                .set("role", roleName)
                .queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(context.getMember().getRoles(), role)) {
            context.makeWarning(context.i18n("higherInTheHierarchy.user"))
                .set("role", role.getName())
                .queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(context.getGuild().getSelfMember().getRoles(), role)) {
            context.makeWarning(context.i18n("higherInTheHierarchy.bot"))
                .set("role", role.getName())
                .queue();
            return false;
        }

        try {
            updateAutorole(transformer, context, role.getId());

            context.makeSuccess(context.i18n("enabled"))
                .set("role", role.getName())
                .queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }
        return true;
    }

    private boolean disableAutoRole(CommandMessage context, GuildTransformer transformer) {
        try {
            transformer.setAutorole(null);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("autorole", null));

            context.makeWarning(context.i18n("disabledNow")).queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    private PlaceholderMessage sendCurrentAutoRole(CommandMessage context, GuildTransformer transformer) {
        if (transformer.getAutorole() == null) {
            return context.makeWarning(context.i18n("disabled"));
        }

        Role role = context.getGuild().getRoleById(transformer.getAutorole());
        if (role == null) {
            try {
                updateAutorole(transformer, context, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
                AvaIre.getLogger().error(ex.getMessage(), ex);
            }
            return context.makeWarning(context.i18n("disabled"));
        }

        return context.makeSuccess(context.i18n("status"))
            .set("role", role.getName());
    }

    private void updateAutorole(GuildTransformer transformer, CommandMessage context, String value) throws SQLException {
        transformer.setAutorole(value);
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> statement.set("autorole", value));
    }
}
