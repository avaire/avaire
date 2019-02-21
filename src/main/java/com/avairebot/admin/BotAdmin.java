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

package com.avairebot.admin;

import com.avairebot.AvaIre;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class BotAdmin {

    private static final Logger log = LoggerFactory.getLogger(BotAdmin.class);
    private static final AdminUser nullUser = new AdminUser(AdminType.USER);

    private final Set<AdminUser> botAdmins;
    private final AvaIre avaire;

    public BotAdmin(AvaIre avaire, Set<String> botAdmins) {
        this.avaire = avaire;
        this.botAdmins = new HashSet<>();

        for (String userId : botAdmins) {
            try {
                this.botAdmins.add(new AdminUser(
                    Long.parseLong(userId), AdminType.BOT_ADMIN
                ));
            } catch (NumberFormatException e) {
                log.warn("{} is an invalid bot ID, the ID have not been added to the bot admin whitelist.",
                    userId
                );
            }
        }
    }

    /**
     * Gets the admin user matching the given user ID, this method acts as
     * a shortcut for calling {@link #getUserById(String, boolean)} with
     * the boolean argument set to false.
     *
     * @param userId The ID of the user that should be returned.
     * @return The bot admin user for the given user ID.
     */
    @Nonnull
    public AdminUser getUserById(@Nullable String userId) {
        return getUserById(userId, false);
    }

    /**
     * Gets the admin user matching the given user ID, the {@code skipRoleCheck} argument
     * will be used to determine if the bot admin role should be checked for or not.
     *
     * @param userId        The ID of the user that should be returned.
     * @param skipRoleCheck Determines if the bot admin check should be preformed or not.
     * @return The bot admin user for the given user ID.
     */
    @Nonnull
    public AdminUser getUserById(@Nullable String userId, boolean skipRoleCheck) {
        if (userId == null) {
            return nullUser;
        }

        try {
            return getUserById(Long.parseLong(userId), skipRoleCheck);
        } catch (NumberFormatException e) {
            return nullUser;
        }
    }

    /**
     * Gets the admin user matching the given user ID, this method acts as
     * a shortcut for calling {@link #getUserById(long, boolean)} with
     * the boolean argument set to false.
     *
     * @param userId The ID of the user that should be returned.
     * @return The bot admin user for the given user ID.
     */
    @Nonnull
    public AdminUser getUserById(long userId) {
        return getUserById(userId, false);
    }

    /**
     * Gets the admin user matching the given user ID, the {@code skipRoleCheck} argument
     * will be used to determine if the bot admin role should be checked for or not.
     *
     * @param userId        The ID of the user that should be returned.
     * @param skipRoleCheck Determines if the bot admin check should be preformed or not.
     * @return The bot admin user for the given user ID.
     */
    @Nonnull
    public AdminUser getUserById(long userId, boolean skipRoleCheck) {
        AdminUser user = getUserFromBotAdminSet(userId);
        if (user != null) {
            return user;
        }
        return skipRoleCheck ?
            nullUser : new AdminUser(userId, getRoleAdminType(userId));
    }

    /**
     * Gets the user from the bot admins set with
     * a matching user ID to the one given.
     *
     * @param userId The user ID to get te admin user for.
     * @return Possibly-null, the user matching the given user id.
     */
    @Nullable
    private AdminUser getUserFromBotAdminSet(long userId) {
        for (AdminUser user : botAdmins) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    /**
     * Checks the given user ID to see if a user with the ID exists on the
     * AvaIre Central server, and if the user has the Bot Admin role.
     *
     * @param userId The user ID that should be checked if they're a role admin.
     * @return <code>True</code> if the user has the bot admin role on the AvaIre Central
     * support server, <code>False</code> otherwise.
     */
    private AdminType getRoleAdminType(long userId) {
        Role role = avaire.getShardManager().getRoleById(DiscordConstants.BOT_ADMIN_EXCEPTION_ROLE);
        if (role == null) {
            return AdminType.USER;
        }

        Member member = role.getGuild().getMemberById(userId);
        if (member == null) {
            return AdminType.USER;
        }

        if (!RoleUtil.hasRole(member, role)) {
            return AdminType.USER;
        }
        return AdminType.ROLE_ADMIN;
    }
}
