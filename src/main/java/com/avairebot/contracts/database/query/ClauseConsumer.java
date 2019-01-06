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

package com.avairebot.contracts.database.query;

import com.avairebot.database.query.NestedClause;
import com.avairebot.database.query.QueryBuilder;

@FunctionalInterface
public interface ClauseConsumer {

    /**
     * Builds the nested clauses by wrapping the nested clause builder into the build
     * type, used by {@link QueryBuilder#where(ClauseConsumer) where},
     * {@link QueryBuilder#andWhere(ClauseConsumer)}, and
     * {@link QueryBuilder#orWhere(ClauseConsumer)}.
     *
     * @param builder The nested clause builder for the current type.
     */
    void build(NestedClause builder);
}
