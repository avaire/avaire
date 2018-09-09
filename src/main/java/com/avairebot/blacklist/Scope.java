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

public enum Scope {

    USER(0, 'U'),
    GUILD(1, 'G');

    private final int id;
    private final char prefix;

    Scope(int id, char prefix) {
        this.id = id;
        this.prefix = prefix;
    }

    public static Scope fromId(int id) {
        for (Scope scope : values()) {
            if (scope.getId() == id) {
                return scope;
            }
        }
        return null;
    }

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

    public int getId() {
        return id;
    }

    public char getPrefix() {
        return prefix;
    }
}
