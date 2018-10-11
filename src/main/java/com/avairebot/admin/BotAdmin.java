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

import javax.annotation.Nullable;
import java.util.Set;

public class BotAdmin {

    private final Set<String> botAdmins;
    private final AvaIre avaire;

    public BotAdmin(AvaIre avaire, Set<String> botAdmins) {
        this.avaire = avaire;
        this.botAdmins = botAdmins;
    }

    /**
     * Checks if the given string exists in the {@link #botAdmins bot admins} set.
     *
     * @param string The string that should be checked if it exists in the bot admin set.
     * @return <code>True</code> if the given string is not <code>NULL</code> and exists
     * in the bot admins list, <code>False</code> otherwise.
     */
    public AdminType isAdmin(@Nullable String string) {
        return isAdmin(string, false);
    }

    /**
     * Checks if the given string exists in the {@link #botAdmins bot admins} set, if the
     * <code>checkRole</code> option is set to true, the string will also be tried to
     * matched with a user ID, and checked to see if the user has the special
     * {@link DiscordConstants#BOT_ADMIN_EXCEPTION_ROLE bot admin role}.
     *
     * @param string    The string that should be checked if it exists in the bot admin set.
     * @param checkRole Determines if the role check should run as well.
     * @return <code>True</code> if the given string is not <code>NULL</code> and either
     * exists in the bot admins list, or if the given string is a user ID and the
     * user has the special admin role, <code>False</code> otherwise.
     */
    public AdminType isAdmin(@Nullable String string, boolean checkRole) {
        if (string == null) {
            return AdminType.USER;
        }

        if (botAdmins.contains(string)) {
            return AdminType.BOT_ADMIN;
        }

        if (!checkRole) {
            return AdminType.USER;
        }

        try {
            return isRoleAdmin(Long.parseLong(string));
        } catch (NumberFormatException e) {
            return AdminType.USER;
        }
    }

    /**
     * Checks the given user ID to see if a user with the ID exists on the
     * AvaIre Central server, and if the user has the Bot Admin role.
     *
     * @param userId The user ID that should be checked if they're a role admin.
     * @return <code>True</code> if the user has the bot admin role on the AvaIre Central
     * support server, <code>False</code> otherwise.
     */
    public AdminType isRoleAdmin(long userId) {
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
