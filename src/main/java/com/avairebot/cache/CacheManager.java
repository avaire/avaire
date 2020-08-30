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

import com.avairebot.AvaIre;
import com.avairebot.contracts.cache.CacheAdapter;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CacheManager extends CacheAdapter {

    protected final AvaIre avaire;

    /**
     * Creates the new cache manager, allowing Ava and
     * plugins to store things temporarily.
     *
     * @param avaire The main AvaIre application instance.
     */
    public CacheManager(AvaIre avaire) {
        this.avaire = avaire;
    }

    @Override
    public boolean put(String token, Object value, int seconds) {
        return getAdapter(null).put(token, value, seconds);
    }

    /**
     * Store an item in the cache for a given number of seconds.
     *
     * @param type    The cache type to store the value in.
     * @param token   The cache item token
     * @param value   The item that should be stored in the cache
     * @param seconds The amount of seconds the item should be stored for
     * @return true if the cache was save correctly, false otherwise.
     */
    public boolean put(CacheType type, String token, Object value, int seconds) {
        return getAdapter(type).put(token, value, seconds);
    }

    @Override
    public Object remember(String token, int seconds, Supplier<Object> closure) {
        return getAdapter(null).remember(token, seconds, closure);
    }

    /**
     * Get an item from the cache, or store the default value.
     *
     * @param type    The cache type to store the value in.
     * @param token   The cache item token
     * @param seconds The amount of seconds the item should be stored for
     * @param closure The closure that should be invoked if the cache doesn't exists
     * @return the object that exists in the cache, if the cache token is empty the result of the closure will be retruend instead.
     */
    public Object remember(CacheType type, String token, int seconds, Supplier<Object> closure) {
        return getAdapter(type).remember(token, seconds, closure);
    }

    @Override
    public boolean forever(String token, Object value) {
        return getAdapter(null).forever(token, value);
    }

    /**
     * Store an item in the cache indefinitely.
     *
     * @param type  The cache type to store the value in.
     * @param token The cache item token
     * @param value The item that should be stored in the cache
     * @return true if the cache was save correctly, false otherwise.
     */
    public boolean forever(CacheType type, String token, Object value) {
        return getAdapter(type).forever(token, value);
    }

    @Override
    public Object get(String token) {
        return getAdapter(null).get(token);
    }

    /**
     * Retrieve an item from the cache by key.
     *
     * @param type  The cache type to get the value from.
     * @param token The cache item token
     * @return the result of the cache token if it exists, null otherwise.
     */
    public Object get(CacheType type, String token) {
        return getAdapter(type).get(token);
    }

    @Override
    public CacheItem getRaw(String token) {
        return getAdapter(null).getRaw(token);
    }

    /**
     * Retrieve an item from the cache in raw form by key.
     *
     * @param type  The cache type to get the value from.
     * @param token The cache item token
     * @return the raw cache item object.
     */
    public CacheItem getRaw(CacheType type, String token) {
        return getAdapter(type).getRaw(token);
    }

    @Override
    public boolean has(String token) {
        return getAdapter(null).has(token);
    }

    /**
     * Determine if an item exists in the cache.
     *
     * @param type  The cache type to key should be checked in.
     * @param token The cache item token
     * @return true if the cache exits, false otherwise.
     */
    public boolean has(CacheType type, String token) {
        return getAdapter(type).has(token);
    }

    @Override
    public CacheItem forget(String token) {
        return getAdapter(null).forget(token);
    }

    /**
     * Remove an item from the cache.
     *
     * @param type  The cache type that the key should be forgotten from.
     * @param token The cache item token
     * @return true if the cache was forgotten correctly, false otherwise.
     */
    public CacheItem forget(CacheType type, String token) {
        return getAdapter(type).forget(token);
    }

    @Override
    public boolean flush() {
        return getAdapter(null).flush();
    }

    /**
     * Remove all items from the given cache type cache.
     *
     * @param type The cache type that should have all of its keys flushed.
     * @return true if the cache was emptied, false otherwise.
     */
    public boolean flush(CacheType type) {
        return getAdapter(type).flush();
    }

    /**
     * Gets the cache adapter for the given cache type.
     *
     * @param type The cache type that should be returned.
     * @return The cache adapter matching the given cache type, or
     * the default cache adapter of <code>NULL</code> is given.
     */
    public CacheAdapter getAdapter(@Nullable CacheType type) {
        if (type != null) {
            return type.getAdapter();
        }
        return CacheType.getDefault().getAdapter();
    }
}
