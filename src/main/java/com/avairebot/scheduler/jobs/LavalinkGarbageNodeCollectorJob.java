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

package com.avairebot.scheduler.jobs;

import com.avairebot.AvaIre;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLink;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class LavalinkGarbageNodeCollectorJob extends Job {

    public static final LoadingCache<Long, Integer> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build(new CacheLoader<Long, Integer>() {
            @Override
            public Integer load(@Nonnull Long key) throws Exception {
                return 0;
            }
        });

    public LavalinkGarbageNodeCollectorJob(AvaIre avaire) {
        super(avaire, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (!LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            return;
        }

        synchronized (LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getLinks()) {
            Iterator<JdaLink> iterator = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getLinks().iterator();

            while (iterator.hasNext()) {
                JdaLink next = iterator.next();
                if (!LavalinkManager.LavalinkManagerHolder.lavalink.isLinkInState(next, Link.State.DESTROYING)) {
                    continue;
                }

                Integer value = CacheUtil.getUncheckedUnwrapped(cache, next.getGuildIdLong());
                cache.put(next.getGuildIdLong(), value + 1);

                if (value > 3) {
                    iterator.remove();
                }
            }
        }
    }
}
