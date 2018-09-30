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
import com.avairebot.blacklist.BlacklistEntity;
import com.avairebot.blacklist.Scope;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;

public class SyncBlacklistMetricsTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        if (avaire.getBlacklist() == null) {
            register();
            return;
        }

        int servers = 0,
            users = 0;


        for (BlacklistEntity entity : avaire.getBlacklist().getBlacklistEntities()) {
            if (!entity.isBlacklisted()) {
                continue;
            }

            if (entity.getScope().equals(Scope.GUILD)) {
                servers++;
            } else {
                users++;
            }
        }

        Metrics.blacklist.labels("servers").set(servers);
        Metrics.blacklist.labels("users").set(users);
    }

    private void register() {
        Metrics.blacklist.labels("servers").set(0);
        Metrics.blacklist.labels("users").set(0);
    }
}
