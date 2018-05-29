package com.avairebot.utilities;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class CacheUtil {

    public static <K, V> Object getUncheckedUnwrapped(Cache<K, V> cache, K key, Callable<V> loader) {
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

    public static <K, V> V getUncheckedUnwrapped(LoadingCache<K, V> cache, K key) {
        try {
            return cache.getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());

            // Will never run.
            throw new IllegalStateException(e);
        }
    }
}
