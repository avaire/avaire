/*
 * Copyright (c) 2019.
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
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MuteRoleCommand extends Command {

    public MuteRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Mute Role Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to set, create, or reset the mute role used for mute related commands on the server, the mute role is what is assigned to users when they're muted, preventing them from talking or speaking in voice channels.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command set <role>` - Sets the mute role to the given role.",
            "`:command create-role` - Creates a new role called \"Muted\".",
            "`:command reset` - Resets the mute role, disabling the mute feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command set @Talk Too Much` - Sets the muted role to the \"Talk Too Much\" role.",
            "`:command set 601361125220810765` - Sets the muted role to the given ID.",
            "`:command create-role` - Creates and sets up a new muted role.",
            "`:command reset` - Resets the mute role."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            MuteCommand.class,
            UnmuteCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("muterole");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server,general.manage_roles",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (args.length == 0) {
            return sendMutedRole(context, guildTransformer);
        }

        switch (args[0].toLowerCase()) {
            case "set":
                return setMutedRole(context, guildTransformer, Arrays.copyOfRange(args, 1, args.length));

            case "create-role":
                return createMutedRole(context, guildTransformer);

            case "reset":
                return resetRole(context, guildTransformer);
        }

        return sendErrorMessage(context, "errors.missingArgument", "option");
    }

    private boolean sendMutedRole(CommandMessage context, GuildTransformer guildTransformer) {
        if (guildTransformer.getMuteRole() == null) {
            context.makeInfo(context.i18n("noMuteRoleSet"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .queue();

            return true;
        }

        Role role = context.getGuild().getRoleById(guildTransformer.getMuteRole());
        if (role == null) {
            context.makeInfo(context.i18n("noMuteRoleSet"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .queue();

            return true;
        }

        context.makeInfo(context.i18n("usingRoleMessage"))
            .set("role", role.getAsMention())
            .queue();

        return true;
    }

    private boolean setMutedRole(CommandMessage context, GuildTransformer guildTransformer, String[] args) {
        Role role = MentionableUtil.getRole(context.getMessage(), args);
        if (role == null) {
            return sendErrorMessage(context, context.i18n("roleDoesntExists", String.join(" ", args)));
        }

        if (!context.getGuild().getSelfMember().canInteract(role)) {
            return sendErrorMessage(context, context.i18n("rolePositionedHigher", role.getAsMention()));
        }

        return updateMutedRole(context, guildTransformer, role.getId());
    }

    private boolean createMutedRole(CommandMessage context, GuildTransformer guildTransformer) {
        List<Role> muted = context.getGuild().getRolesByName("Muted", true);
        if (!muted.isEmpty()) {
            return updateMutedRole(context, guildTransformer, muted.get(0).getId());
        }

        if (!context.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            return sendErrorMessage(context, context.i18n("cantCreateRoleDueToPermissions"));
        }

        context.getGuild().getController().createRole()
            .setName("Muted")
            .setColor(Color.decode("#e91b6a"))
            .setPermissions(
                Permission.MESSAGE_READ,
                Permission.MESSAGE_HISTORY
            )
            .queue(role -> updateMutedRole(context, guildTransformer, role.getId()));

        return true;
    }

    private boolean resetRole(CommandMessage context, GuildTransformer guildTransformer) {
        return updateMutedRole(context, guildTransformer, null);
    }

    private boolean updateMutedRole(CommandMessage context, GuildTransformer guildTransformer, String value) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("mute_role", value));

            guildTransformer.setMuteRole(value);

            context.makeSuccess(
                context.i18n(value == null ? "roleHasBeenRemoved" : "nowUsingRole")
            ).set("roleId", value).queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error(e.getMessage(), e);
            context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();

            return false;
        }
    }
}
