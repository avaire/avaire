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

package com.avairebot.utilities;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class CacheUtil {

    /**
     * Gets the given key from the given cache if it exists, if no entry
     * was found in the cache with the given key, the loader will be
     * used to populate the cache key instead.
     * <p>
     * The method improves upon the conventional "if cached,
     * return; otherwise create, cache and return" pattern.
     * <p>
     * For more information on how this works, checkout
     * the {@link Cache#get(Object, Callable)} method.
     *
     * @param cache  The cache that the key should be loaded from.
     * @param key    The key that should be retrieved from the cache.
     * @param loader The loader that should be invoked in the event
     *               that the key doesn't exist in the cache.
     * @return The object matching the given cache value from either the cache itself, or the loader.
     */
    public static <K, V> Object getUncheckedUnwrapped(@Nonnull Cache<K, V> cache, @Nonnull K key, @Nonnull Callable<V> loader) {
        try {
            return cache.get(key, loader);
        } catch (ExecutionException e) {
            throw new RuntimeException("Cache loader threw exception", e);
        } catch (UncheckedExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());

            // Will never run.
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the key from the given cache loader, if the cache doesn't exists
     * the cache loader will call it's loader function to fill in the
     * value for the given key and then return that instead.
     * <p>
     * For more information on how this works, checkout
     * the {@link LoadingCache#getUnchecked(Object)} method.
     *
     * @param cache The cache that the key should be loaded from.
     * @param key   The key that should be retrieved from the cache.
     * @return The object matching the given cache value.
     */
    public static <K, V> V getUncheckedUnwrapped(@Nonnull LoadingCache<K, V> cache, @Nonnull K key) {
        try {
            return cache.getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());

            // Will never run.
            throw new IllegalStateException(e);
        }
    }
}
