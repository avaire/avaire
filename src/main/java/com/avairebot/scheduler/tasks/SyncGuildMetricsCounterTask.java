package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;

public class SyncGuildMetricsCounterTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        if (!avaire.areWeReadyYet() || !hasLoadedGuilds(avaire)) {
            return;
        }

        Metrics.memoryTotal.set(Runtime.getRuntime().totalMemory());
        Metrics.memoryUsed.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        Metrics.guilds.set(avaire.getShardEntityCounter().getGuilds());

        for (Region region : Region.values()) {
            Metrics.geoTracker.labels(region.getName()).set(0);
        }

        for (JDA shard : avaire.getShardManager().getShards()) {
            for (Guild guild : shard.getGuilds()) {
                Metrics.geoTracker.labels(guild.getRegion().getName()).inc();
            }
        }
    }

    private boolean hasLoadedGuilds(AvaIre avaire) {
        if (avaire.getShardManager().getShards().size() != avaire.getSettings().getShardCount()) {
            return false;
        }

        for (JDA shard : avaire.getShardManager().getShards()) {
            if (shard.getGuildCache().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
