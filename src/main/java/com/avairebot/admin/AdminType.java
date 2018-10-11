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

import com.avairebot.commands.CommandPriority;

import javax.annotation.Nullable;

public enum AdminType {

    /**
     * Represents a user who is defined as a bot admin through the
     * config, giving them access to all system commands.
     */
    BOT_ADMIN(true, CommandPriority.SYSTEM),

    /**
     * Represents a user who isn't defined as a bot admin through the
     * config, but has the "Bot Administrator" role on the official
     * AvaIre support server, giving them access to run a select
     * few system commands.
     */
    ROLE_ADMIN(true, CommandPriority.SYSTEM_ROLE),

    /**
     * Represents a user who is not a bot admin.
     */
    USER(false);

    private final boolean admin;
    private final CommandPriority commandScope;

    AdminType(boolean admin) {
        this(admin, null);
    }

    AdminType(boolean admin, CommandPriority commandScope) {
        this.admin = admin;
        this.commandScope = commandScope;
    }

    /**
     * Determines if the admin type gives the
     * user access to run admin commands.
     *
     * @return <code>True</code> if the admin type allows running
     * admin commands, <code>False</code> otherwise.
     */
    public boolean isAdmin() {
        return admin;
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
        return commandScope;
    }
}
