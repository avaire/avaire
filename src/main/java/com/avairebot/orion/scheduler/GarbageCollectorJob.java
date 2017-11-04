package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.cache.adapters.MemoryAdapter;
import com.avairebot.orion.contracts.scheduler.Job;

public class GarbageCollectorJob extends Job {

    public GarbageCollectorJob(Orion orion) {
        super(orion);
    }

    @Override
    public void run() {
        MemoryAdapter adapter = (MemoryAdapter) orion.getCache().getAdapter(CacheType.MEMORY);

        for (String key : adapter.getCacheKeys()) {
            if (!adapter.has(key)) {
                adapter.forget(key);
            }
        }
    }
}
