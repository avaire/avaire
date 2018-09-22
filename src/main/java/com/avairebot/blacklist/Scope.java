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

package com.avairebot.blacklist;

import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Scope {

    /**
     * The user scope, used for blacklisting users.
     */
    USER(0, 'U', "User"),

    /**
     * The guild/server scope, used for blacklisting servers.
     */
    GUILD(1, 'G', "Server");

    private final int id;
    private final char prefix;
    private final String name;

    Scope(int id, char prefix, String name) {
        this.id = id;
        this.prefix = prefix;
        this.name = name;
    }

    /**
     * Gets the scope from the given ID, if no valid
     * ID was given, null will be returned instead.
     *
     * @param id The ID that the scope should match.
     * @return Possibly-null, the scope matching the given ID.
     */
    public static Scope fromId(int id) {
        for (Scope scope : values()) {
            if (scope.getId() == id) {
                return scope;
            }
        }
        return null;
    }

    /**
     * /**
     * Parses the given string to a valid scope by comparing
     * the string to scope prefixes and scope IDs.
     *
     * @param string The string that should be parsed.
     * @return Possible-null, the scope matching the given string.
     */
    @Nullable
    public static Scope parse(@Nonnull String string) {
        int parsedInt = NumberUtil.parseInt(string, -1);
        for (Scope scope : values()) {
            if (string.toUpperCase().charAt(0) == scope.getPrefix()) {
                return scope;
            }

            if (parsedInt == scope.getId()) {
                return scope;
            }
        }
        return null;
    }

    /**
     * Gets the ID of the scope.
     *
     * @return The ID of the scope.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the prefix of the scope.
     *
     * @return The prefix of the scope.
     */
    public char getPrefix() {
        return prefix;
    }

    /**
     * Gets the name of the scope
     *
     * @return The name of the scope.
     */
    public String getName() {
        return name;
    }
}
