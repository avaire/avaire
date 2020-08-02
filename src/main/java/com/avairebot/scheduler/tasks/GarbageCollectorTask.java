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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;

import com.avairebot.blacklist.Ratelimit;
import com.avairebot.cache.CacheType;
import com.avairebot.cache.adapters.MemoryAdapter;
import com.avairebot.commands.administration.MuteRoleCommand;
import com.avairebot.contracts.commands.InteractionCommand;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.handlers.adapter.JDAStateEventAdapter;
import com.avairebot.handlers.adapter.MessageEventAdapter;


public class GarbageCollectorTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        // Clears the user cache set for DM info messages, this will reset
        // the list, allowing users to get the DM info message again.
        MessageEventAdapter.hasReceivedInfoMessageInTheLastMinute.clear();

        // Remove cache entries from the memory cache adapter
        // if the keys are still stored by has expired.
        MemoryAdapter adapter = (MemoryAdapter) avaire.getCache().getAdapter(CacheType.MEMORY);
        adapter.getCacheKeys().removeIf(key -> !adapter.has(key));


        // Cleans up caches that are not hit very often, so
        // instead of just keeping the entities in the
        // cache, we can clean them up here.
        cleanupCache();
    }

    /**
     * Goes through some of the less used caches and
     * cleans up any entities that have expired.
     */
    private void cleanupCache() {
        // blacklist-ratelimit
        synchronized (Ratelimit.cache) {
            Ratelimit.cache.cleanUp();
        }

        // interaction-lottery
        synchronized (InteractionCommand.cache) {
            InteractionCommand.cache.cleanUp();
        }

        // autorole
        synchronized (JDAStateEventAdapter.cache) {
            JDAStateEventAdapter.cache.cleanUp();
        }

        // muterole
        synchronized (MuteRoleCommand.cache) {
            MuteRoleCommand.cache.cleanUp();
        }
    }
}
