package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.metrics.Metrics;
import com.avairebot.shard.AvaireShard;

import java.util.concurrent.TimeUnit;

public class UpdateWebsocketHeartbeatMetricsJob extends Job {

    public UpdateWebsocketHeartbeatMetricsJob(AvaIre avaire) {
        super(avaire, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        for (AvaireShard shard : avaire.getShards()) {
            Metrics.websocketHeartbeat.labels("Shard " + shard.getShardId())
                .set(shard.getJDA().getPing());
        }
    }
}
