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

import com.avairebot.middleware.MiddlewareStack;
import com.avairebot.permissions.Permissions;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionCheck {

    private final static Logger log = LoggerFactory.getLogger(PermissionCheck.class);

    /**
     * The arguments given to the middleware that should
     * contain the permission type and permissions.
     */
    private final String[] args;

    /**
     * Determines if the user has the Administrator permissions,
     * if they do have that we can skip some checks.
     */
    private final boolean isUserAdmin;

    /**
     * The message from the event that invoked the permission check.
     */
    private final Message message;

    /**
     * The type of permission check that should be done.
     */
    private final PermissionType type;

    /**
     * A list of permissions that are missing for the bot.
     */
    private final List<Permissions> missingBotPermissions = new ArrayList<>();

    /**
     * A list of permissions that are missing for the user.
     */
    private final List<Permissions> missingUserPermissions = new ArrayList<>();

    /**
     * Determines if the user has at least one of the required permissions.
     */
    private boolean userHasAtleastOne = false;

    /**
     * Determines if the bot has at least one of the required permissions.
     */
    private boolean botHasAtleastOne = false;

    /**
     * Creates a new permission check instance for the current message.
     *
     * @param message The message that invoked the middleware stack.
     * @param args    The arguments parsed to the middleware.
     */
    public PermissionCheck(@Nonnull Message message, String[] args) {
        this.isUserAdmin = message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission());
        this.type = PermissionType.fromName(args[0]);
        this.message = message;
        this.args = args;

        if (isUserAdmin) {
            userHasAtleastOne = true;
        }
    }

    /**
     * Checks the middleware stack permissions.
     *
     * @param stack The middleware stack that was built for the current message.
     * @return <code>True</code> if the check ran successfully, <code>False</code> if an invalid permission node was given.
     */
    public boolean check(@Nonnull MiddlewareStack stack) {
        String[] permissions = Arrays.copyOfRange(args, 1, args.length);

        for (String permissionNode : permissions) {
            Permissions permission = Permissions.fromNode(permissionNode);
            if (permission == null) {
                log.warn(String.format("Invalid permission node given for the \"%s\" command: %s", stack.getCommand().getName(), permissionNode));
                return false;
            }

            if (!isUserAdmin && type.isCheckUser() && !message.getMember().hasPermission(permission.getPermission())) {
                missingUserPermissions.add(permission);
            }

            if (type.isCheckBot()) {
                if (!message.getGuild().getSelfMember().hasPermission(permission.getPermission())) {
                    missingBotPermissions.add(permission);
                    continue;
                }

                if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), permission.getPermission())) {
                    missingBotPermissions.add(permission);
                }
            }
        }

        botHasAtleastOne = missingBotPermissions.size() < permissions.length;
        if (!userHasAtleastOne) {
            userHasAtleastOne = missingUserPermissions.size() < permissions.length;
        }

        return true;
    }

    /**
     * Gets the type of the permission check.
     *
     * @return The type of the permission check.
     */
    public PermissionType getType() {
        return type;
    }

    /**
     * A list of permissions that are missing for the bot.
     *
     * @return A list of permission missing for the bot.
     */
    public List<Permissions> getMissingBotPermissions() {
        return missingBotPermissions;
    }

    /**
     * A list of permissions that are missing for the user.
     *
     * @return A list of permission missing for the user.
     */
    public List<Permissions> getMissingUserPermissions() {
        return missingUserPermissions;
    }

    /**
     * Returns true if the bot has at least one of the required permissions.
     *
     * @return <code>True</code> if the bot has at least one of the required permissions, <code>False</code> otherwise.
     */
    public boolean botHasAtleastOne() {
        return botHasAtleastOne;
    }

    /**
     * Returns true if the user has at least one of the required permissions.
     *
     * @return <code>True</code> if the user has at least one of the required permissions, <code>False</code> otherwise.
     */
    public boolean userHasAtleastOne() {
        return userHasAtleastOne;
    }

    /**
     * Checks if the {@link #missingBotPermissions missing bot permissions}, and the
     * {@link #missingUserPermissions missing user permissions} list are both empty.
     *
     * @return Checks if bot the user and bot permissions list are empty.
     */
    public boolean isEmpty() {
        return missingBotPermissions.isEmpty()
            && missingUserPermissions.isEmpty();
    }
}
