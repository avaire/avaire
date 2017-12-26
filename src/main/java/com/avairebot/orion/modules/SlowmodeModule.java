package com.avairebot.orion.modules;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheItem;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.utilities.NumberUtil;

public class SlowmodeModule {

    public static boolean isThrottled(Orion orion, String fingerprint, int limit, int decay) {
        CacheItem cacheItem = orion.getCache().getAdapter(CacheType.MEMORY).getRaw(fingerprint);

        if (cacheItem == null) {
            orion.getCache().getAdapter(CacheType.MEMORY).put(fingerprint, 1, decay);
            return false;
        }

        int value = NumberUtil.parseInt(cacheItem.getValue().toString(), 0);
        if (value++ >= limit) {
            return true;
        }

        orion.getCache().getAdapter(CacheType.MEMORY).put(fingerprint, value, decay);
        return false;
    }
}
