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

public enum CommandPriority {

    HIDDEN(2),
    SYSTEM(1),
    IGNORED(0),
    LOWEST(1),
    LOW(2),
    NORMAL(3),
    HIGH(4),
    HIGHEST(5);

    private final int priority;

    CommandPriority(int priority) {
        this.priority = priority;
    }

    public boolean isGreaterThan(CommandPriority commandPriority) {
        return priority > commandPriority.priority;
    }
}
