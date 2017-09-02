package com.avairebot.orion.cache;

import com.avairebot.orion.time.Carbon;

public class CacheItem {
    private final String key;
    private final Object value;
    private final Carbon time;

    public CacheItem(String key, Object value, Carbon time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Carbon getTime() {
        return time;
    }
}
