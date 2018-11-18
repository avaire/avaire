/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;

import java.lang.management.ManagementFactory;

public class SyncJDAMetricsCounterTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        Metrics.uptime.labels("dynamic").set(ManagementFactory.getRuntimeMXBean().getUptime());

        Metrics.memoryTotal.set(Runtime.getRuntime().totalMemory());
        Metrics.memoryUsed.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        if (!avaire.areWeReadyYet() || !hasLoadedGuilds(avaire)) {
            return;
        }

        Metrics.guilds.set(avaire.getShardEntityCounter().getGuilds());
        Metrics.users.set(avaire.getShardEntityCounter().getUsers());
        Metrics.channels.labels("text").set(avaire.getShardEntityCounter().getTextChannels());
        Metrics.channels.labels("voice").set(avaire.getShardEntityCounter().getVoiceChannels());

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
        if (avaire.getSettings().getShardCount() != -1
            && avaire.getShardManager().getShards().size() != avaire.getSettings().getShardCount()) {
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
