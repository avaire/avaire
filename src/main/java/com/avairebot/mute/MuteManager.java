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

package com.avairebot.mute;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.language.I18n;
import com.avairebot.modlog.ModlogType;
import com.avairebot.time.Carbon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MuteManager {

    private final Logger log = LoggerFactory.getLogger(MuteManager.class);
    private final HashMap<Long, HashSet<MuteContainer>> mutes = new HashMap<>();

    private final AvaIre avaire;

    public MuteManager(AvaIre avaIre) {
        this.avaire = avaIre;

        syncWithDatabase();
    }

    public void registerMute(String caseId, long guildId, long userId, @Nullable Carbon expiresAt) throws SQLException {
        if (!mutes.containsKey(guildId)) {
            mutes.put(guildId, new HashSet<>());
        }

        if (isMuted(guildId, userId)) {
            unregisterMute(guildId, userId);
        }

        avaire.getDatabase().newQueryBuilder(Constants.MUTE_TABLE_NAME)
            .insert(statement -> {
                statement.set("guild_id", guildId);
                statement.set("modlog_id", caseId);
                statement.set("expires_in", expiresAt);
            });

        mutes.get(guildId).add(new MuteContainer(guildId, userId, expiresAt));
    }

    public void unregisterMute(long guildId, long userId) throws SQLException {
        if (!mutes.containsKey(guildId)) {
            return;
        }

        final boolean[] removedEntities = {false};
        synchronized (mutes) {
            mutes.get(guildId).removeIf(next -> {
                if (!next.isSame(guildId, userId)) {
                    return false;
                }

                if (next.getSchedule() != null) {
                    next.cancelSchedule();
                }

                removedEntities[0] = true;
                return true;
            });
        }

        if (removedEntities[0]) {
            cleanupMutes(guildId, userId);
        }
    }

    public boolean isMuted(long guildId, long userId) {
        if (!mutes.containsKey(guildId)) {
            return false;
        }

        for (MuteContainer container : mutes.get(guildId)) {
            if (container.isSame(guildId, userId)) {
                return true;
            }
        }
        return false;
    }

    public int getTotalAmountOfMutes() {
        int totalMutes = 0;
        for (Map.Entry<Long, HashSet<MuteContainer>> entry : mutes.entrySet()) {
            totalMutes += entry.getValue().size();
        }
        return totalMutes;
    }

    public HashMap<Long, HashSet<MuteContainer>> getMutes() {
        return mutes;
    }

    public void syncWithDatabase() {
        log.info("Syncing mutes with the database...");

        String query = I18n.format("SELECT `{1}`.`guild_id`, `{1}`.`target_id`, `{0}`.`expires_in` FROM `{0}` INNER JOIN `{1}` ON `{0}`.`modlog_id` = `{1}`.`modlogCase` WHERE `{0}`.`modlog_id` = `{1}`.`modlogCase` AND `{0}`.`guild_id` = `{1}`.`guild_id`;",
            Constants.MUTE_TABLE_NAME, Constants.LOG_TABLE_NAME
        );

        try {
            int size = getTotalAmountOfMutes();
            for (DataRow row : avaire.getDatabase().query(query)) {
                long guildId = row.getLong("guild_id");

                if (!mutes.containsKey(guildId)) {
                    mutes.put(guildId, new HashSet<>());
                }

                mutes.get(guildId).add(new MuteContainer(
                    row.getLong("guild_id"),
                    row.getLong("target_id"),
                    row.getTimestamp("expires_in")
                ));
            }

            log.info("Syncing complete! {} mutes entries was found that has not expired yet",
                getTotalAmountOfMutes() - size
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cleanupMutes(long guildId, long userId) throws SQLException {
        Collection collection = avaire.getDatabase().newQueryBuilder(Constants.MUTE_TABLE_NAME)
            .select(Constants.MUTE_TABLE_NAME + ".modlog_id as id")
            .innerJoin(
                Constants.LOG_TABLE_NAME,
                Constants.MUTE_TABLE_NAME + ".modlog_id",
                Constants.LOG_TABLE_NAME + ".modlogCase"
            )
            .where(Constants.LOG_TABLE_NAME + ".guild_id", guildId)
            .andWhere(Constants.LOG_TABLE_NAME + ".target_id", userId)
            .andWhere(Constants.MUTE_TABLE_NAME + ".guild_id", guildId)
            .andWhere(builder -> builder
                .where(Constants.LOG_TABLE_NAME + ".type", ModlogType.MUTE.getId())
                .orWhere(Constants.LOG_TABLE_NAME + ".type", ModlogType.TEMP_MUTE.getId())
            ).get();

        if (!collection.isEmpty()) {
            String query = String.format("DELETE FROM `%s` WHERE `guild_id` = ? AND `modlog_id` = ?",
                Constants.MUTE_TABLE_NAME
            );

            avaire.getDatabase().queryBatch(query, statement -> {
                for (DataRow row : collection) {
                    statement.setLong(1, guildId);
                    statement.setString(2, row.getString("id"));
                    statement.addBatch();
                }
            });
        }
    }
}
