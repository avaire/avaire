package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;

public class UpdateWebsocketHeartbeatMetricsTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        for (JDA shard : avaire.getShardManager().getShards()) {
            Metrics.websocketHeartbeat.labels("Shard " + shard.getShardInfo().getShardId())
                .set(shard.getPing());
        }
    }
}
