/*
 * Copyright (c) 2019.
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

import java.util.LinkedHashMap;

public class CategoryDataContext {

    /**
     * The global prefix used by all commands registered to the category.
     */
    private final String prefix;

    /**
     * The map of commands linked to the category, where the key is the class
     * name of the command, and the value is the actual command data context.
     */
    private final LinkedHashMap<String, CommandDataContext> commands;

    CategoryDataContext(String prefix, LinkedHashMap<String, CommandDataContext> commands) {
        this.prefix = prefix;
        this.commands = commands;
    }

    /**
     * Gets the category command prefix, the prefix is used
     * by all commands registered to the category.
     *
     * @return The category command prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets all the commands registered to the category, where the key is
     * the class name, and the value is the command data context.
     *
     * @return The command data context map.
     */
    public LinkedHashMap<String, CommandDataContext> getCommands() {
        return commands;
    }
}
