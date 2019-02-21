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

package com.avairebot.admin;

import com.avairebot.commands.CommandPriority;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class AdminUser {

    private final long userId;
    private final AdminType type;

    AdminUser(long userId, AdminType type) {
        this.userId = userId;
        this.type = type;
    }

    AdminUser(AdminType type) {
        this(-1L, type);
    }

    /**
     * Gets the ID of the user the admin user object was created for,
     * if the admin user were created without a link to a user,
     * the ID will be set to {@code -1}.
     *
     * @return The ID of the user the admin user is linked to.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the command priority scope for the current admin type, if the
     * admin type does not allow {@link #isAdmin() admin commands}
     * being run this will always return <code>NULL</code>.
     *
     * @return Possibly-null, if the admin type allows admin commands
     * being run, the scope for the admin type will be returned.
     */
    @Nullable
    public CommandPriority getCommandScope() {
        return type.getCommandScope();
    }

    /**
     * Checks if the admin user is a bot admin globally, this is only true if
     * the user has their ID stored in the bot access field in the config.
     *
     * @return {@code True} if the user is a bot admin globally, {@code False} otherwise.
     */
    public boolean isGlobalAdmin() {
        return type.equals(AdminType.BOT_ADMIN);
    }

    /**
     * Checks if the admin user is a role admin, this is only true if the user
     * has the special bot admin role on the official support server.
     *
     * @return {@code True} if the user is a bot role admin, {@code False} otherwise.
     */
    public boolean isRoleAdmin() {
        return type.equals(AdminType.ROLE_ADMIN);
    }

    /**
     * Checks if the admin user is actually a bot admin by checking if {@link #isGlobalAdmin()}
     * or {@link #isRoleAdmin()} returns {@code True}.
     *
     * @return {@code True} if the user is a bot admin, {@code False} otherwise.
     */
    public final boolean isAdmin() {
        return isGlobalAdmin() || isRoleAdmin();
    }
}
