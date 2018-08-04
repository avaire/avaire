package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.AudioSession;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.cache.CacheType;
import com.avairebot.cache.adapters.MemoryAdapter;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.handlers.adapter.MessageEventAdapter;
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
        AudioHandler.getDefaultAudioHandler().musicManagers.entrySet().removeIf(this::musicManagerFilter);
        AudioHandler.getDefaultAudioHandler().audioSessions.entrySet().removeIf(this::audioSessionFilter);
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
}
