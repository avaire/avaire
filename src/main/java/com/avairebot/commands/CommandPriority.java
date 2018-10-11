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

    HIDDEN(3),
    SYSTEM(2, true),
    SYSTEM_ROLE(1, true),
    IGNORED(0),
    LOWEST(1),
    LOW(2),
    NORMAL(3),
    HIGH(4),
    HIGHEST(5);

    private final int priority;
    private final boolean system;

    CommandPriority(int priority) {
        this(priority, false);
    }

    CommandPriority(int priority, boolean system) {
        this.priority = priority;
        this.system = system;
    }

    public boolean isGreaterThan(CommandPriority commandPriority) {
        return priority > commandPriority.priority;
    }

    public boolean isSystem() {
        return system;
    }
}
