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
