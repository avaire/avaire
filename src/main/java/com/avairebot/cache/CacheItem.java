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

package com.avairebot.cache;

public class CacheItem {

    private final String key;
    private final Object value;
    private final long time;

    /**
     * Creates a new cache item entity with the given key, value, and expire time.
     *
     * @param key   The key that the cache item should be stored under.
     * @param value The raw value that the cache item holds.
     * @param time  The unix timestamp in milliseconds representing when the cache item expires.
     */
    public CacheItem(String key, Object value, long time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    /**
     * Gets the key the cache item is stored under.
     *
     * @return The key the cache item is stored under.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the raw value of the cache item.
     *
     * @return The raw value of the cache item.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the unix timestamp in milliseconds for when the cache item
     * should expire, if the cache item is set to last forever,
     * the time will always be set to <code>-1</code>.
     *
     * @return The unix timestamp in milliseconds for when the cache item
     * expires, or <code>-1</code> if it is set to last forever.
     */
    public long getTime() {
        return time;
    }

    /**
     * Checks if the cache item has expired, if the cache item is set
     * to last forever this will always return <code>Falser</code>.
     *
     * @return <code>True</code> if the cache item has expired, <code>False</code> otherwise.
     */
    public boolean isExpired() {
        return !lastForever() && getTime() > System.currentTimeMillis();
    }

    /**
     * Checks if the cache item lasts forever.
     *
     * @return <code>True</code> if the cache item should last forever, <code>False</code> otherwise.
     */
    public boolean lastForever() {
        return time == -1;
    }
}
