package com.avairebot.middleware.permission;

import javax.annotation.Nonnull;

public enum PermissionType {

    USER(true, false),
    BOT(false, true),
    ALL(true, true);

    private final boolean checkUser;
    private final boolean checkBot;

    PermissionType(boolean checkUser, boolean checkBot) {
        this.checkUser = checkUser;
        this.checkBot = checkBot;
    }

    /**
     * Gets the permission type matching the given name, if no valid permission type
     * was given, {@link #USER PermissionType.USER} will be returned instead.
     *
     * @param name The name that should be matched against the permission types.
     * @return Never-null, the permission type that matches the given name,
     * or the {@link #USER user} permission type.
     */
    @Nonnull
    public static PermissionType fromName(String name) {
        for (PermissionType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return PermissionType.USER;
    }

    /**
     * Determines if the user should be checked.
     *
     * @return <code>True</code> if users should be checked for
     * this permission type, <code>False</code> otherwise.
     */
    public boolean isCheckUser() {
        return checkUser;
    }

    /**
     * Determines if the bot should be checked.
     *
     * @return <code>True</code> if bots should be checked for
     * this permission type, <code>False</code> otherwise.
     */
    public boolean isCheckBot() {
        return checkBot;
    }
}
