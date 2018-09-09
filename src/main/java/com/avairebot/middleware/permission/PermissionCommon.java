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

package com.avairebot.middleware.permission;

import com.avairebot.permissions.Permissions;

import javax.annotation.Nullable;

public class PermissionCommon {

    /**
     * Formats the permission middleware description message
     * if only one argument was given to the middleware.
     *
     * @param arguments The first argument given to the middleware.
     * @return Possibly-null, the formatted string if the given argument is a valid permission node.
     */
    @Nullable
    public static String formatWithOneArgument(String arguments) {
        Permissions node = Permissions.fromNode(arguments);
        if (node != null) {
            return String.format("**The `%s` permission is required to use this command!**",
                node.getPermission().getName()
            );
        }
        return null;
    }
}
