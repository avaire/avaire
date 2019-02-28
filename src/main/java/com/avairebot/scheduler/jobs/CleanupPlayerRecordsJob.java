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
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This job will periodically go through XP records and mark any XP
 * record as inactive if the user is no longer on the server,
 * or the bot have been kicked from the server.
 */
@SuppressWarnings("unused")
public class CleanupPlayerRecordsJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(CleanupPlayerRecordsJob.class);

    public CleanupPlayerRecordsJob(AvaIre avaire) {
        super(avaire, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        List<InactiveUser> inactiveUsers = getInactiveUsers();
        if (inactiveUsers.isEmpty()) {
            return;
        }

        try {
            String query = String.format(
                "UPDATE `%s` SET `active` = 0 WHERE `user_id` = ? AND `guild_id` = ?",
                Constants.PLAYER_EXPERIENCE_TABLE_NAME
            );

            log.debug("Starting \"Player Cleanup\" job with query: " + query);

            avaire.getDatabase().queryBatch(query, statement -> {
                for (InactiveUser entity : inactiveUsers) {
                    statement.setString(1, entity.userId);
                    statement.setString(2, entity.guildId);
                    statement.addBatch();
                }
            });

            log.debug("Finished \"Player Cleanup\" job, updated {} records in the process", inactiveUsers.size());
        } catch (SQLException e) {
            log.error("An SQL exception was thrown while updating player experience: ", e);
        }
    }

    private List<InactiveUser> getInactiveUsers() {
        try {
            List<InactiveUser> inactiveUsers = new ArrayList<>();
            Guild guild = null;

            for (DataRow dataRow : getUsersFromDatabase()) {
                if (guild == null || !guild.getId().equalsIgnoreCase(dataRow.getString("guild_id"))) {
                    try {
                        guild = avaire.getShardManager().getGuildById(dataRow.getString("guild_id"));
                    } catch (Exception ignored) {
                        inactiveUsers.add(createInactiveUser(dataRow));
                        continue;
                    }
                }

                if (guild == null) {
                    inactiveUsers.add(createInactiveUser(dataRow));
                    continue;
                }

                try {
                    if (guild.getMemberById(dataRow.getString("user_id")) == null) {
                        inactiveUsers.add(createInactiveUser(dataRow));
                    }
                } catch (Exception ignored) {
                    inactiveUsers.add(createInactiveUser(dataRow));
                }
            }

            return inactiveUsers;
        } catch (SQLException e) {
            log.error("Failed to fetch active users for cleanup, error: {}", e.getMessage(), e);

            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
    }

    private Collection getUsersFromDatabase() throws SQLException {
        return avaire.getDatabase()
            .newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
            .select("user_id", "guild_id")
            .where("active", 1)
            .orderBy("guild_id")
            .get();
    }

    private InactiveUser createInactiveUser(DataRow row) {
        return new InactiveUser(
            row.getString("user_id"),
            row.getString("guild_id")
        );
    }

    private class InactiveUser {

        private final String userId;
        private final String guildId;

        InactiveUser(String userId, String guildId) {
            this.userId = userId;
            this.guildId = guildId;
        }
    }
}
