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
        if (!encode) {
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
