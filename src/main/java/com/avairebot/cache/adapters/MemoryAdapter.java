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

package com.avairebot.cache.adapters;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheItem;
import com.avairebot.contracts.cache.CacheAdapter;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class MemoryAdapter extends CacheAdapter {

    private final Map<String, CacheItem> cache = new WeakHashMap<>();

    @Override
    public boolean put(String token, Object value, int seconds) {
        cache.put(token, new CacheItem(token, value, System.currentTimeMillis() + (seconds * 1000)));
        return true;
    }

    @Override
    public Object remember(String token, int seconds, Supplier<Object> closure) {
        if (has(token)) {
            return get(token);
        }

        try {
            CacheItem item = new CacheItem(token, closure.get(), System.currentTimeMillis() + (seconds * 1000));
            cache.put(token, item);

            return item.getValue();
        } catch (Exception e) {
            AvaIre.getLogger().error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean forever(String token, Object value) {
        cache.put(token, new CacheItem(token, value, -1));

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
        return cache.getOrDefault(token, null);
    }

    @Override
    public boolean has(String token) {
        return cache.containsKey(token) && cache.get(token).isExpired();
    }

    @Override
    public CacheItem forget(String token) {
        return cache.remove(token);
    }

    @Override
    public boolean flush() {
        cache.clear();
        return true;
    }

    /**
     * Gets the cache keys currently in the memory cache.
     *
     * @return The cache keys currently in the memory cache.
     */
    public Set<String> getCacheKeys() {
        return cache.keySet();
    }
}
