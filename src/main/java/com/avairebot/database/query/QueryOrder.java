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

package com.avairebot.database.query;

public class QueryOrder {

    /**
     * The field that should be used for the query order.
     */
    private final String field;

    /**
     * The type of query order that should be used.
     */
    private final String type;

    /**
     * Determines if the field is raw SQL or not.
     */
    private final boolean rawSQL;

    /**
     * Creates a new query order with the provided field and type.
     *
     * @param field The field to use in the query order
     * @param type  The type to use in the query order
     */
    public QueryOrder(String field, String type) {
        this.rawSQL = false;

        this.field = field;
        this.type = type;
    }

    /**
     * Creates a new query order with the provided field, type and rawSQL value.
     *
     * @param field  The field to use in the query order
     * @param type   The type to use in the query order
     * @param rawSQL The raw SQL value to use
     */
    public QueryOrder(String field, String type, boolean rawSQL) {
        this.field = field;
        this.type = type;
        this.rawSQL = rawSQL;
    }

    /**
     * Gets the field used in the query order.
     *
     * @return the field used in the query order.
     */
    public String getField() {
        return field;
    }

    /**
     * Gets the query order type.
     *
     * @return the query order type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets whether the field is a raw SQL value or not.
     *
     * @return whether the field is a raw SQL value or not.
     */
    public boolean isRawSQL() {
        return rawSQL;
    }
}
