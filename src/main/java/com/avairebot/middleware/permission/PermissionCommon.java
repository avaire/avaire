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

import javax.annotation.Nonnull;
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
    public static String formatWithOneArgument(PermissionType type, String arguments) {
        Permissions node = Permissions.fromNode(arguments);
        if (node != null) {
            return String.format(getPermissionTypeMessage(type),
                node.getPermission().getName()
            );
        }
        return null;
    }

    /**
     * Gets the permission message for the given type, using a singular naming of
     * the permissions, if the permission checks has more than one permission in
     * it, the {@link #getPermissionTypeMessage(PermissionType, boolean)}
     * method can be used directly to overwrite the multiple.
     *
     * @param type The type of message that should be returned.
     * @return Never-null, the permission message matching the given type.
     */
    @Nonnull
    public static String getPermissionTypeMessage(PermissionType type) {
        return getPermissionTypeMessage(type, false);
    }

    /**
     * Gets the permission message for the given type, generating the permission
     * message  using either singular or multiple languages depending on if
     * there are one or more permissions if the permission check.
     *
     * @param type     The type of message that should be returned.
     * @param multiple Determines if the check should return a the message
     *                 using singular or multiple language.
     * @return Never-null, the permission message matching the given type.
     */
    @Nonnull
    public static String getPermissionTypeMessage(PermissionType type, boolean multiple) {
        switch (type) {
            case USER:
                return "**You need the `%s` permission:multiple to use this command!**"
                    .replace(":multiple", multiple ? "s" : "");

            case BOT:
                return "**The bot needs the `%s` permission:multiple is run this command!**"
                    .replace(":multiple", multiple ? "s" : "");

            default:
                return "**You and the bot both needs the `%s` permission:multiple to use this command!**"
                    .replace(":multiple", multiple ? "s" : "");
        }
    }
}
