package com.avairebot.cache;

public class CacheItem {

    private final String key;
    private final Object value;
    private final long time;

    public CacheItem(String key, Object value, long time) {
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

    public long getTime() {
        return time;
    }

    public boolean isExpired() {
        return !lastForever() && getTime() > System.currentTimeMillis();
    }

    public boolean lastForever() {
        return time == -1;
    }
}
