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

package com.avairebot.contracts.cache;

import com.avairebot.cache.CacheItem;

import java.util.function.Supplier;

public abstract class CacheAdapter {

    /**
     * Store an item in the cache for a given number of seconds.
     *
     * @param token   The cache item token
     * @param value   The item that should be stored in the cache
     * @param seconds The amount of seconds the item should be stored for
     * @return true if the cache was save correctly, false otherwise.
     */
    public abstract boolean put(String token, Object value, int seconds);

    /**
     * Get an item from the cache, or store the default value.
     *
     * @param token   The cache item token
     * @param seconds The amount of seconds the item should be stored for
     * @param closure The closure that should be invoked if the cache doesn't exists
     * @return the object that exists in the cache, if the cache token is empty the result of the closure will be retruend instead.
     */
    public abstract Object remember(String token, int seconds, Supplier<Object> closure);

    /**
     * Store an item in the cache indefinitely.
     *
     * @param token The cache item token
     * @param value The item that should be stored in the cache
     * @return true if the cache was save correctly, false otherwise.
     */
    public abstract boolean forever(String token, Object value);

    /**
     * Retrieve an item from the cache by key.
     *
     * @param token The cache item token
     * @return the result of the cache token if it exists, null otherwise.
     */
    public abstract Object get(String token);

    /**
     * Retrieve an item from the cache in raw form by key.
     *
     * @param token The cache item token
     * @return the raw cache item object.
     */
    public abstract CacheItem getRaw(String token);

    /**
     * Determine if an item exists in the cache.
     *
     * @param token The cache item token
     * @return true if the cache exits, false otherwise.
     */
    public abstract boolean has(String token);

    /**
     * Remove an item from the cache.
     *
     * @param token The cache item token
     * @return true if the cache was forgotten correctly, false otherwise.
     */
    public abstract CacheItem forget(String token);

    /**
     * Remove all items from the cache.
     *
     * @return true if the cache was emptied, false otherwise.
     */
    public abstract boolean flush();
}
