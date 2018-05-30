package com.avairebot.cache;

import com.avairebot.AvaIre;
import com.avairebot.contracts.cache.CacheAdapter;

import java.util.function.Supplier;

public class CacheManager extends CacheAdapter {

    protected final AvaIre avaire;

    public CacheManager(AvaIre avaire) {
        this.avaire = avaire;
    }

    @Override
    public boolean put(String token, Object value, int seconds) {
        return getAdapter(null).put(token, value, seconds);
    }

    public boolean put(CacheType type, String token, Object value, int seconds) {
        return getAdapter(type).put(token, value, seconds);
    }

    @Override
    public Object remember(String token, int seconds, Supplier<Object> closure) {
        return getAdapter(null).remember(token, seconds, closure);
    }

    public Object remember(CacheType type, String token, int seconds, Supplier<Object> closure) {
        return getAdapter(type).remember(token, seconds, closure);
    }

    @Override
    public boolean forever(String token, Object value) {
        return getAdapter(null).forever(token, value);
    }

    public boolean forever(CacheType type, String token, Object value) {
        return getAdapter(type).forever(token, value);
    }

    @Override
    public Object get(String token) {
        return getAdapter(null).get(token);
    }

    public Object get(CacheType type, String token) {
        return getAdapter(type).get(token);
    }

    @Override
    public CacheItem getRaw(String token) {
        return getAdapter(null).getRaw(token);
    }

    public CacheItem getRaw(CacheType type, String token) {
        return getAdapter(type).getRaw(token);
    }

    @Override
    public boolean has(String token) {
        return getAdapter(null).has(token);
    }

    public boolean has(CacheType type, String token) {
        return getAdapter(type).has(token);
    }

    @Override
    public CacheItem forget(String token) {
        return getAdapter(null).forget(token);
    }

    public CacheItem forget(CacheType type, String token) {
        return getAdapter(type).forget(token);
    }

    @Override
    public boolean flush() {
        return getAdapter(null).flush();
    }

    public boolean flush(CacheType type) {
        return getAdapter(type).flush();
    }

    public CacheAdapter getAdapter(CacheType type) {
        if (type != null) {
            return type.getAdapter();
        }
        return CacheType.getDefault().getAdapter();
    }
}
