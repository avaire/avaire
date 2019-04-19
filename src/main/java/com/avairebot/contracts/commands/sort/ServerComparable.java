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

package com.avairebot.contracts.commands.sort;

import com.avairebot.commands.system.ServersCommand;

import java.util.List;

public interface ServerComparable {

    /**
     * Sorts the given list of servers for the {@link ServersCommand.SortTypes sort types},
     * allowing users to sort the servers using different filters.
     *
     * @param servers The list of servers that should be sorted.
     */
    void sort(List<ServersCommand.Server> servers);
}
