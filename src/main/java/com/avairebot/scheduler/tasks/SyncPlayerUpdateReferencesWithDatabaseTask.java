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
import com.avairebot.database.controllers.PlayerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SyncPlayerUpdateReferencesWithDatabaseTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(SyncPlayerUpdateReferencesWithDatabaseTask.class);

    @Override
    public void handle(AvaIre avaire) {
        if (PlayerController.getPlayerQueue().isEmpty()) {
            return;
        }

        Map<Long, PlayerController.PlayerUpdateReference> playerQueue;
        synchronized (PlayerController.getPlayerQueue()) {
            playerQueue = new HashMap<>(PlayerController.getPlayerQueue());
            PlayerController.getPlayerQueue().clear();
        }

        String query = String.format("UPDATE `%s` SET `avatar` = ?, `username` = ?, `discriminator` = ? WHERE `user_id` = ?",
            Constants.PLAYER_EXPERIENCE_TABLE_NAME
        );

        log.debug("Starting \"Player Reference\" update task with query: " + query);

        try {
            avaire.getDatabase().queryBatch(query, preparedStatement -> {
                for (Map.Entry<Long, PlayerController.PlayerUpdateReference> entity : playerQueue.entrySet()) {
                    preparedStatement.setString(1, entity.getValue().getAvatar());
                    preparedStatement.setString(2, entity.getValue().getUsername());
                    preparedStatement.setString(3, entity.getValue().getDiscriminator());
                    preparedStatement.setString(4, entity.getKey().toString());
                    preparedStatement.addBatch();
                }
            });

            log.debug("Finished \"Player Reference\" task, updated {} records in the process", playerQueue.size());
        } catch (SQLException e) {
            log.error("An SQL exception was thrown while updating player references: ", e);
        }
    }
}
