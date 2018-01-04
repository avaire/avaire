package com.avairebot.modules;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheItem;
import com.avairebot.cache.CacheType;
import com.avairebot.utilities.NumberUtil;

public class SlowmodeModule {

    public static boolean isThrottled(AvaIre avaire, String fingerprint, int limit, int decay) {
        CacheItem cacheItem = avaire.getCache().getAdapter(CacheType.MEMORY).getRaw(fingerprint);

        if (cacheItem == null) {
            avaire.getCache().getAdapter(CacheType.MEMORY).put(fingerprint, 1, decay);
            return false;
        }

        int value = NumberUtil.parseInt(cacheItem.getValue().toString(), 0);
        if (value++ >= limit) {
            return true;
        }

        avaire.getCache().getAdapter(CacheType.MEMORY).put(fingerprint, value, decay);
        return false;
    }
}
