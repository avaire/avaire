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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ChangeableStatement {

    private final QueryBuilder query;
    private final Map<String, Object> items;

    ChangeableStatement(QueryBuilder query) {
        this.query = query;
        this.items = new HashMap<>();
    }

    public ChangeableStatement set(String key, Object value) {
        return set(key, value, false);
    }

    public ChangeableStatement set(String key, Object value, boolean encode) {
        if (!encode || value == null) {
            items.put(key, value);
            return this;
        }

        items.put(key, "base64:" + new String(
            Base64.getEncoder().encode(value.toString().getBytes())
        ));

        return this;
    }

    public ChangeableStatement setRaw(String key, String rawSQL) {
        items.put(key, "RAW:" + rawSQL);
        return this;
    }

    public String toSQL() {
        return query.toSQL();
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public Map<String, Object> getItems() {
        return items;
    }
}
