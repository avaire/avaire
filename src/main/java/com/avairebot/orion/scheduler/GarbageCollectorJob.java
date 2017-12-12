package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.cache.adapters.MemoryAdapter;
import com.avairebot.orion.contracts.scheduler.Job;

import java.util.Map;

public class GarbageCollectorJob extends Job {

    public GarbageCollectorJob(Orion orion) {
        super(orion);
    }

    @Override
    public void run() {
        MemoryAdapter adapter = (MemoryAdapter) orion.getCache().getAdapter(CacheType.MEMORY);

        adapter.getCacheKeys().removeIf(key -> !adapter.has(key));

        for (Map.Entry<Long, GuildMusicManager> entry : AudioHandler.MUSIC_MANAGER.entrySet()) {
            if (entry.getValue() == null) {
                AudioHandler.MUSIC_MANAGER.remove(entry.getKey());
                continue;
            }

            if (entry.getValue().getLastActiveMessage() == null) {
                continue;
            }

            if (entry.getValue().getLastActiveMessage().getGuild().getAudioManager().isConnected()) {
                continue;
            }

            if (!adapter.has(getCacheFingerprint(entry.getKey()))) {
                adapter.put(getCacheFingerprint(entry.getKey()), 0, 120);
                continue;
            }

            adapter.forget(getCacheFingerprint(entry.getKey()));

            AudioHandler.MUSIC_MANAGER.remove(entry.getKey());
        }
    }

    private String getCacheFingerprint(Long guildId) {
        return "garbage-collector.music-queue." + guildId;
    }
}
