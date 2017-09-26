package com.avairebot.orion.database.query;

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
        items.put(key, value);
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
