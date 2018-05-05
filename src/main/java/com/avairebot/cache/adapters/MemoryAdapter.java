package com.avairebot.cache.adapters;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheItem;
import com.avairebot.contracts.cache.CacheAdapter;
import com.avairebot.contracts.cache.CacheClosure;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class MemoryAdapter extends CacheAdapter {

    private final Map<String, CacheItem> CACHES = new WeakHashMap<String, CacheItem>();

    @Override
    public boolean put(String token, Object value, int seconds) {
        CACHES.put(token, new CacheItem(token, value, System.currentTimeMillis() + (seconds * 1000)));
        return true;
    }

    @Override
    public Object remember(String token, int seconds, CacheClosure closure) {
        if (has(token)) {
            return get(token);
        }

        try {
            CacheItem item = new CacheItem(token, closure.run(), System.currentTimeMillis() + (seconds * 1000));
            CACHES.put(token, item);

            return item.getValue();
        } catch (Exception e) {
            AvaIre.getLogger().error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean forever(String token, Object value) {
        CACHES.put(token, new CacheItem(token, value, -1));

        return true;
    }

    @Override
    public Object get(String token) {
        if (!has(token)) {
            return null;
        }

        CacheItem item = getRaw(token);
        if (item == null) {
            return null;
        }
        return item.getValue();
    }

    @Override
    public CacheItem getRaw(String token) {
        if (!has(token)) {
            return null;
        }
        return CACHES.getOrDefault(token, null);
    }

    @Override
    public boolean has(String token) {
        return CACHES.containsKey(token) && CACHES.get(token).isExpired();
    }

    @Override
    public CacheItem forget(String token) {
        return CACHES.remove(token);
    }

    @Override
    public boolean flush() {
        CACHES.clear();
        return true;
    }

    public Set<String> getCacheKeys() {
        return CACHES.keySet();
    }
}
