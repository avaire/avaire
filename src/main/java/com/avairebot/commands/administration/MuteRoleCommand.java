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
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MuteRoleCommand extends Command {

    public static final Cache<Long, Long> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(150, TimeUnit.SECONDS)
        .build();

    public MuteRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Mute Role Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to set, create, setup, or reset the mute role used for mute related commands on the server, the mute role is what is assigned to users when they're muted, preventing them from talking or speaking in voice channels.\n\nWhen creating a mute role via the bot, the bot will create a new role called `Muted`, and then add permission overrides to all text and voice channels the bot has access to, preventing the role from talking or speaking in the channels. If an existing role is set, the permissions can also be automatically setup using the `setup-permissions` option.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command set <role>` - Sets the mute role to the given role.",
            "`:command create-role` - Creates a new role called \"Muted\".",
            "`:command setup-permissions` - Sets up the permissions for the mute role.",
            "`:command reset` - Resets the mute role, disabling the mute feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command set @Talk Too Much` - Sets the muted role to the \"Talk Too Much\" role.",
            "`:command set 601361125220810765` - Sets the muted role to the given ID.",
            "`:command setup-permissions` - Sets up the permissions for the current mute role.",
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
            case "create-roles":
                return createMutedRole(context, guildTransformer);

            case "setup-permission":
            case "setup-permissions":
                return setupPermissions(context, guildTransformer);

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

            return false;
        }

        Role role = context.getGuild().getRoleById(guildTransformer.getMuteRole());
        if (role == null) {
            context.makeInfo(context.i18n("noMuteRoleSet"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .queue();

            return false;
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

        try {
            updateMutedRole(context, guildTransformer, role.getId());

            context.makeSuccess(context.i18n("nowUsingRole"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .set("role", role.getAsMention())
                .queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error(e.getMessage(), e);
            context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
        }

        return false;
    }

    private boolean createMutedRole(CommandMessage context, GuildTransformer guildTransformer) {
        Long cacheTimestamp = cache.getIfPresent(context.getGuild().getIdLong());
        if (cacheTimestamp != null) {
            return handleCooldown(context, cacheTimestamp);
        }

        List<Role> muted = context.getGuild().getRolesByName("Muted", true);
        if (!muted.isEmpty()) {
            try {
                Role role = muted.get(0);

                updateMutedRole(context, guildTransformer, role.getId());

                context.makeSuccess(context.i18n("nowUsingExistingRole"))
                    .set("command", generateCommandTrigger(context.getMessage()))
                    .set("role", role.getAsMention())
                    .queue();

                return true;
            } catch (SQLException e) {
                AvaIre.getLogger().error(e.getMessage(), e);
                context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();

                return false;
            }
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
            .queue(role -> {
                cache.put(context.getGuild().getIdLong(), System.currentTimeMillis());

                for (Channel channel : context.getGuild().getChannels()) {
                    if (!context.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                        continue;
                    }

                    channel.putPermissionOverride(role).setDeny(
                        Permission.CREATE_INSTANT_INVITE.getRawValue() + (
                            channel.getType().equals(ChannelType.TEXT)
                                ? Permission.ALL_TEXT_PERMISSIONS
                                : Permission.ALL_VOICE_PERMISSIONS
                        )
                    ).queue();
                }

                try {
                    updateMutedRole(context, guildTransformer, role.getId());

                    context.makeSuccess(context.i18n("roleHasBeenCreated"))
                        .set("role", role.getAsMention())
                        .queue();
                } catch (SQLException e) {
                    AvaIre.getLogger().error(e.getMessage(), e);
                    context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
                }
            });

        return true;
    }

    private boolean setupPermissions(CommandMessage context, GuildTransformer guildTransformer) {
        Long cacheTimestamp = cache.getIfPresent(context.getGuild().getIdLong());
        if (cacheTimestamp != null) {
            return handleCooldown(context, cacheTimestamp);
        }

        if (guildTransformer.getMuteRole() == null) {
            context.makeInfo(context.i18n("noMuteRoleSet"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .queue();

            return false;
        }

        Role role = context.getGuild().getRoleById(guildTransformer.getMuteRole());
        if (role == null) {
            context.makeInfo(context.i18n("noMuteRoleSet"))
                .set("command", generateCommandTrigger(context.getMessage()))
                .queue();

            return false;
        }

        cache.put(context.getGuild().getIdLong(), System.currentTimeMillis());

        int channelsSkipped = 0;
        int channelsModified = 0;
        int channelsNotModified = 0;

        for (Channel channel : context.getGuild().getChannels()) {
            if (channel.getType().equals(ChannelType.CATEGORY)) {
                continue;
            }

            if (!context.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                channelsSkipped++;
                continue;
            }

            long rawPermissions = Permission.CREATE_INSTANT_INVITE.getRawValue();
            switch (channel.getType()) {
                case TEXT:
                    rawPermissions |= Permission.ALL_TEXT_PERMISSIONS;
                    break;

                case VOICE:
                    rawPermissions |= Permission.ALL_VOICE_PERMISSIONS;
                    break;
            }

            if (rawPermissions == 0) {
                // If the channel is a text or voice type, we'll just skip it.
                channelsSkipped++;
                continue;
            }

            PermissionOverride permissionOverride = channel.getPermissionOverride(role);
            if (permissionOverride != null && permissionOverride.getDeniedRaw() == rawPermissions) {
                channelsNotModified++;
                continue;
            }

            channelsModified++;
            channel.putPermissionOverride(role).setDeny(rawPermissions).queue();
        }

        context.makeSuccess("The override permissions have been setup for the :role role.\n**:modified** channels has been modified, **:notModified** channels were already setup, and **:skipped** channels where skipped due to missing permissions.")
            .set("notModified", channelsNotModified)
            .set("modified", channelsModified)
            .set("skipped", channelsSkipped)
            .set("role", role.getAsMention())
            .queue();

        return true;
    }

    private boolean resetRole(CommandMessage context, GuildTransformer guildTransformer) {
        try {
            updateMutedRole(context, guildTransformer, null);

            context.makeSuccess(context.i18n("roleHasBeenRemoved")).queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error(e.getMessage(), e);
            context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
        }

        return false;
    }

    private boolean handleCooldown(CommandMessage context, long timestamp) {
        int secondsDiff = Math.toIntExact((timestamp / 1000L) - ((System.currentTimeMillis() / 1000L) - 150));

        context.makeWarning(context.i18n("onCooldown"))
            .set("cooldown", Carbon.now().addSeconds(secondsDiff).diffForHumans(true))
            .queue();

        return false;
    }

    private void updateMutedRole(CommandMessage context, GuildTransformer guildTransformer, String value) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> statement.set("mute_role", value));

        guildTransformer.setMuteRole(value);
    }
}
