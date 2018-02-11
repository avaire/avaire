package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;

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

        for (JDA shard : avaire.getShardManager().getShards()) {
            Metrics.websocketHeartbeat.labels("Shard " + shard.getShardInfo().getShardId())
                .set(shard.getPing());
        }
    }
}
