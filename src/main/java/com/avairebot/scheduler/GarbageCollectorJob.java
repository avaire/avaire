package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.cache.CacheType;
import com.avairebot.cache.adapters.MemoryAdapter;
import com.avairebot.contracts.scheduler.Job;
import lavalink.client.io.Link;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.Map;

public class GarbageCollectorJob extends Job {

    private static int counter = 1;

    public GarbageCollectorJob(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void run() {
        MemoryAdapter adapter = (MemoryAdapter) avaire.getCache().getAdapter(CacheType.MEMORY);

        adapter.getCacheKeys().removeIf(key -> !adapter.has(key));

        for (Map.Entry<Long, GuildMusicManager> entry : AudioHandler.MUSIC_MANAGER.entrySet()) {
            if (entry.getValue() == null) {
                AudioHandler.MUSIC_MANAGER.remove(entry.getKey());
                continue;
            }

            if (entry.getValue().getLastActiveMessage() == null) {
                continue;
            }

            if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
                if (isConnected(LavalinkManager.LavalinkManagerHolder.LAVALINK.getLavalink().getLink(
                    entry.getValue().getLastActiveMessage().getGuild()
                ).getState())) {
                    continue;
                }
            } else {
                if (isConnected(entry.getValue().getLastActiveMessage().getGuild().getAudioManager())) {
                    continue;
                }
            }

            AudioHandler.MUSIC_MANAGER.remove(entry.getKey());
        }

        // 25 seconds
        AudioHandler.AUDIO_SESSION.entrySet()
            .removeIf(next -> (next.getValue().getCreatedAt() + 25000) < System.currentTimeMillis());

        if (counter++ % 60 == 0) {
            System.gc();
        }
    }

    private boolean isConnected(AudioManager audioManager) {
        return audioManager.isConnected() || audioManager.isAttemptingToConnect();
    }

    private boolean isConnected(Link.State state) {
        return state.equals(Link.State.CONNECTED) || state.equals(Link.State.CONNECTING);
    }
}
