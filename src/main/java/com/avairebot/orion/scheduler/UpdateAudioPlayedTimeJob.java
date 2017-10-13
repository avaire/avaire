package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.GuildMusicManager;
import com.avairebot.orion.contracts.scheduler.Job;

import java.util.concurrent.TimeUnit;

public class UpdateAudioPlayedTimeJob extends Job {

    public UpdateAudioPlayedTimeJob(Orion orion) {
        super(orion, 0, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        for (GuildMusicManager manager : AudioHandler.MUSIC_MANAGER.values()) {
            if (manager.getPlayer() == null || manager.getPlayer().isPaused()) {
                continue;
            }

            if (manager.getScheduler().getAudioTrackContainer() == null) {
                continue;
            }

            if (manager.getScheduler().getAudioTrackContainer().getAudioTrack().getInfo().isStream) {
                continue;
            }

            manager.getScheduler().getAudioTrackContainer().incrementPlayedTime();
        }
    }
}
