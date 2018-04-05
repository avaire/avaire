package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;

import java.util.concurrent.TimeUnit;

public class SyncGuildMetricsCounterJob extends Job {

    public SyncGuildMetricsCounterJob(AvaIre avaire) {
        super(avaire, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet() || !hasLoadedGuilds()) {
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

    private boolean hasLoadedGuilds() {
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
