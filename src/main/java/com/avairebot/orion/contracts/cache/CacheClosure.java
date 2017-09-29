package com.avairebot.orion.contracts.cache;

public interface CacheClosure {

    /**
     * Runs the cache closure, caching the result returned from the run method.
     *
     * @return The object that should be cached.
     */
    Object run();
}
