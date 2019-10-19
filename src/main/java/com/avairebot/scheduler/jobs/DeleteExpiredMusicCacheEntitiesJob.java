/*
 * Copyright (c) 2019.
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

package com.avairebot.scheduler.jobs;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.time.Carbon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DeleteExpiredMusicCacheEntitiesJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpiredMusicCacheEntitiesJob.class);

    public DeleteExpiredMusicCacheEntitiesJob(AvaIre avaire) {
        super(avaire, 5, 15, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        final Carbon time = Carbon.now().subSeconds(Math.max(
            Math.max(avaire.getConfig().getInt("audio-cache.default-max-cache-age", 86400), 60),
            avaire.getConfig().getInt("audio-cache.max-persistence-age", 172800)
        ));

        try {
            AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME)
                .where("created_at", "<", time)
                .delete();
        } catch (SQLException e) {
            log.error("Failed to clear old database records, message: {}", e.getMessage(), e);
        }
    }
}
