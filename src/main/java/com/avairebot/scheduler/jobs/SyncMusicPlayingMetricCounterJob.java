package com.avairebot.scheduler.jobs;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;

import java.util.concurrent.TimeUnit;

public class SyncMusicPlayingMetricCounterJob extends Job {

    public SyncMusicPlayingMetricCounterJob(AvaIre avaire) {
        super(avaire, 5, 15, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        handleTask((Task) avaire -> {
            Metrics.musicPlaying.set(AudioHandler.getDefaultAudioHandler().getTotalListenersSize());
        });
    }
}
