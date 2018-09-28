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
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioSession;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.blacklist.Ratelimit;
import com.avairebot.cache.CacheType;
import com.avairebot.cache.adapters.MemoryAdapter;
import com.avairebot.contracts.commands.InteractionCommand;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.handlers.adapter.JDAStateEventAdapter;
import com.avairebot.handlers.adapter.MessageEventAdapter;
import com.avairebot.scheduler.jobs.LavalinkGarbageNodeCollectorJob;
import lavalink.client.io.Link;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.Map;

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

        // Clean music managers and audio sessions by removing
        // them if they have expired or are unused.
        synchronized (AudioHandler.getDefaultAudioHandler().musicManagers) {
            AudioHandler.getDefaultAudioHandler().musicManagers.entrySet().removeIf(this::musicManagerFilter);
        }
        synchronized (AudioHandler.getDefaultAudioHandler().audioSessions) {
            AudioHandler.getDefaultAudioHandler().audioSessions.entrySet().removeIf(this::audioSessionFilter);
        }

        // Cleans up caches that are not hit very often, so
        // instead of just keeping the entities in the
        // cache, we can clean them up here.
        cleanupCache();
    }


    /**
     * Checks if the {@link AudioManager Audio Manager} is connected to a voice channel.
     *
     * @param audioManager The audio manager that should be checked.
     * @return True if the audio manager is connected, false otherwise.
     */
    private boolean isConnected(AudioManager audioManager) {
        return audioManager.isConnected() || audioManager.isAttemptingToConnect();
    }

    /**
     * Checks the {@link Link.State Lavalink State} to see
     * if the client is connected to a voice channel.
     *
     * @param state The Lavalink state that should be checked.
     * @return True if the client is connected, false otherwise.
     */
    private boolean isConnected(Link.State state) {
        return state.equals(Link.State.CONNECTED) || state.equals(Link.State.CONNECTING);
    }

    /**
     * Checks music managers if they should be removed from the cache, music managers
     * are removed if the manager is null, or if the guild has a music manager
     * but is not connected to a voice channel.
     *
     * @param next The music manager entry.
     * @return True the music manager should be removed, false otherwise.
     */
    private boolean musicManagerFilter(Map.Entry<Long, GuildMusicManager> next) {
        if (next.getValue() == null) {
            return true;
        }

        if (next.getValue().getLastActiveMessage() == null) {
            return false;
        }

        // If Lavalink is enabled we'll use the Lavalink link state
        // for the current guild instead of the audio manager.
        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            if (isConnected(LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getLink(
                next.getValue().getLastActiveMessage().getGuild()
            ).getState())) {
                return false;
            }
        } else {
            if (isConnected(next.getValue().getLastActiveMessage().getGuild().getAudioManager())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes audio sessions that has existed for 25 seconds or longer.
     *
     * @param next The audio session entry.
     * @return True if the audio session has existed for 25 seconds or longer, false otherwise.
     */
    private boolean audioSessionFilter(Map.Entry<String, AudioSession> next) {
        return (next.getValue().getCreatedAt() + 25000) < System.currentTimeMillis();
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

        // lavalink-destroy-cleanup
        synchronized (LavalinkGarbageNodeCollectorJob.cache) {
            LavalinkGarbageNodeCollectorJob.cache.cleanUp();
        }
    }
}
