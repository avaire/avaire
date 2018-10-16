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

package com.avairebot.commands;

import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.middleware.MiddlewareHandler;
import com.avairebot.middleware.ThrottleMiddleware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandContainer {

    private static final String DEFAULT_GUILD_LIMIT = "throttle:guild,20,15";
    private static final String DEFAULT_USER_LIMIT = "throttle:user,2,3";

    private final Command command;
    private final Category category;
    private final String sourceUri;
    private final Set<String> triggers;
    private final List<String> middlewares;

    /**
     * Creates a new {@link Command commands} container instance.
     *
     * @param command   The commands that should be assigned the container.
     * @param category  The category for the commands.
     * @param sourceUri The source URI for finding source code of the commands.
     */
    public CommandContainer(@Nonnull Command command, @Nonnull Category category, @Nullable String sourceUri) {
        this.command = command;
        this.category = category;
        this.sourceUri = sourceUri;

        this.triggers = new HashSet<>(command.getTriggers());
        this.middlewares = new ArrayList<>(command.getMiddleware());

        this.registerThrottleMiddlewares();
    }

    /**
     * Get the commands linked to the container.
     *
     * @return The commands linked to the container.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * The commands category that the commands belongs to.
     *
     * @return The commands category that the commands belongs to.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the default prefix from the commands category, this is a shortcut for using
     * {@link #getCategory() container.getCategory()}{@link Category#getPrefix() .getPrefix()}.
     *
     * @return The default prefix for the commands category.
     */
    public String getDefaultPrefix() {
        return category.getPrefix();
    }

    /**
     * Gets the priority for the commands, if the commands priority is
     * {@link CommandPriority#HIDDEN hidden} the commands won't be
     * listed on the help commands.
     *
     * @return The priority for the commands.
     */
    public CommandPriority getPriority() {
        return command.getCommandPriority();
    }

    /**
     * Gets the list of middlewares used by the commands.
     *
     * @return The list of middlewares used by the commands.
     */
    public List<String> getMiddleware() {
        return middlewares;
    }

    /**
     * Gets the commands triggers used to run the commands.
     *
     * @return The commands triggers used to run the commands.
     */
    public Set<String> getTriggers() {
        return triggers;
    }

    /**
     * The source URI used by the {@link com.avairebot.commands.utility.SourceCommand source code commands}
     * to find out where the source code for a given commands can be found.
     *
     * @return Possibly-null, the URI to where the code for the commands can be found.
     */
    @Nullable
    public String getSourceUri() {
        return sourceUri;
    }

    /**
     * Registers the default throttle middlewares unless
     * the commands already has registered one of them.
     */
    private void registerThrottleMiddlewares() {
        if (!hasMiddleware(ThrottleMiddleware.class)) {
            middlewares.add(DEFAULT_USER_LIMIT);
            middlewares.add(DEFAULT_GUILD_LIMIT);
            return;
        }

        boolean addUser = true, addGuild = true;

        for (String middlewareName : middlewares) {
            String[] parts = middlewareName.split(":");

            Middleware middleware = MiddlewareHandler.getMiddleware(parts[0]);
            if (middleware == null || !(middleware instanceof ThrottleMiddleware)) {
                continue;
            }

            ThrottleMiddleware.ThrottleType type = ThrottleMiddleware.ThrottleType
                .fromName(parts[1].split(",")[0]);

            switch (type) {
                case USER:
                    addUser = false;
                    break;

                case GUILD:
                case CHANNEL:
                    addGuild = false;
                    break;
            }
        }

        if (addUser) {
            middlewares.add(DEFAULT_USER_LIMIT);
        }

        if (addGuild) {
            middlewares.add(DEFAULT_GUILD_LIMIT);
        }
    }

    /**
     * Checks if the commands has the given commands middleware.
     *
     * @param clazz The middleware class that should be checke.
     * @return <code>True</code> if the commands has the given middleware, <code>False</code> otherwise.
     */
    private boolean hasMiddleware(@Nonnull Class<? extends Middleware> clazz) {
        String key = MiddlewareHandler.getName(clazz);
        if (key == null) {
            return false;
        }

        for (String middleware : middlewares) {
            if (middleware.toLowerCase().startsWith(key)) {
                return true;
            }
        }
        return false;
    }
}
