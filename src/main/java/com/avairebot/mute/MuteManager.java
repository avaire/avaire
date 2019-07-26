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

    /**
     * Creates the mute manager instance with the given AvaIre
     * application instance, the mute manager will sync the
     * mutes entities from the database into memory.
     *
     * @param avaire The main AvaIre instance.
     */
    public MuteManager(AvaIre avaire) {
        this.avaire = avaire;

        syncWithDatabase();
    }

    /**
     * Registers a mute using the given case ID, guild ID, and user ID,
     * if a null value is given for the expire date, the mute will be
     * registered as a permanent mute, however if a valid carbon
     * instance is given that is set in the future, the mute
     * will automatically be reversed once the time is up.
     * <p>
     * If a mute record already exists for the given guild and user IDs,
     * the record will be unmuted before the new mute is applied, this
     * helps ensure that a user can only have one mute per guild.
     *
     * @param caseId    The ID of the modlog case that triggered the mute action.
     * @param guildId   The ID of the guild the mute should be registered to.
     * @param userId    The ID of the user that was muted.
     * @param expiresAt The time the mute should be automatically unmuted, or {@code NULL} to make the mute permanent.
     * @throws SQLException If the mute fails to be registered with the database, or
     *                      existing mutes for the given guild and user IDs fails
     *                      to be removed before the new mute is registered.
     */
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

    /**
     * Unregisters a mute matching the given guild ID and user ID.
     *
     * @param guildId The ID of the guild the mute should've been registered to.
     * @param userId  The ID of the user that should be unmuted.
     * @throws SQLException If the unmute fails to delete the mute record from the database.
     */
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

    /**
     * Checks if there are any mute record that exists
     * using the given guild and user IDs.
     *
     * @param guildId The ID of the guild that should be checked.
     * @param userId  The ID of the user that should be muted.
     * @return {@code True} if a user with the given ID is muted on a server
     * with the given guild ID, {@code False} otherwise.
     */
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

    /**
     * Gets the total amount of mutes currently stored in memory,
     * this includes permanent and temporary mutes.
     *
     * @return The total amount of mutes stored.
     */
    public int getTotalAmountOfMutes() {
        int totalMutes = 0;
        for (Map.Entry<Long, HashSet<MuteContainer>> entry : mutes.entrySet()) {
            totalMutes += entry.getValue().size();
        }
        return totalMutes;
    }

    /**
     * Gets the map of mutes currently stored, where the key is the guild ID for
     * the mutes, and the value is a set of mute containers, which holds
     * the information about each individual mute.
     *
     * @return The complete map of mutes currently stored.
     */
    public HashMap<Long, HashSet<MuteContainer>> getMutes() {
        return mutes;
    }

    private void syncWithDatabase() {
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
