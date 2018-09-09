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

package com.avairebot.contracts.database.grammar;

import com.avairebot.database.schema.Blueprint;

public abstract class AlterGrammar extends Grammar {

    /**
     * The query formatter method, this is called by the query
     * builder when the query should be built.
     *
     * @param blueprint The blueprint to build the query from.
     * @return the formatted SQL query
     */
    public abstract String format(Blueprint blueprint);

    /**
     * Adds the last few touches the query needs to be ready to be executed.
     *
     * @param builder The query builder to finalize.
     * @return the finalized SQL query
     */
    protected abstract String finalize(Blueprint builder);
}
