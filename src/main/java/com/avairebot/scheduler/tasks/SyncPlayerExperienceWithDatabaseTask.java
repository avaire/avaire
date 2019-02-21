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
import com.avairebot.Constants;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.level.ExperienceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SyncPlayerExperienceWithDatabaseTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(SyncPlayerExperienceWithDatabaseTask.class);

    @Override
    public void handle(AvaIre avaire) {
        if (avaire.getLevelManager().getExperienceQueue().isEmpty()) {
            return;
        }

        List<ExperienceEntity> experienceQueue;
        synchronized (avaire.getLevelManager().getExperienceQueue()) {
            experienceQueue = new ArrayList<>(avaire.getLevelManager().getExperienceQueue());
            avaire.getLevelManager().getExperienceQueue().clear();
        }

        try {
            String query = String.format(
                "UPDATE `%s` SET `experience` = ? + `experience`, `global_experience` = ? + `global_experience` WHERE `user_id` = ? AND `guild_id` = ?",
                Constants.PLAYER_EXPERIENCE_TABLE_NAME
            );

            log.debug("Starting \"Player Experience\" update task with query: " + query);

            avaire.getDatabase().queryBatch(query, statement -> {
                for (ExperienceEntity entity : experienceQueue) {
                    statement.setInt(1, entity.isExcludeLocal() ? 0 : entity.getExperience());
                    statement.setInt(2, entity.getExperience());
                    statement.setString(3, String.valueOf(entity.getUserId()));
                    statement.setString(4, String.valueOf(entity.getGuildId()));
                    statement.addBatch();
                }
            });

            log.debug("Finished \"Player Experience\" task, updated {} records in the process", experienceQueue.size());
        } catch (SQLException e) {
            log.error("An SQL exception was thrown while updating player experience: ", e);
        }
    }
}
