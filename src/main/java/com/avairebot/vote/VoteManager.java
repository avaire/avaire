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

package com.avairebot.vote;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.metrics.Metrics;
import com.avairebot.scheduler.tasks.DrainVoteQueueTask;
import com.avairebot.servlet.routes.PostVote;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

public class VoteManager {

    private static final Logger log = LoggerFactory.getLogger(VoteManager.class);
    private static final DelayQueue<VoteEntity> queue = new DelayQueue<>();
    private static final Map<Long, VoteCacheEntity> voteLog = new HashMap<>();
    private static final VoteMessenger messenger = new VoteMessenger();
    private static long lastCheck = -1;

    private final AvaIre avaire;

    /**
     * Creates the vote manager instance with the given AvaIre
     * application instance, the vote manager will setup all
     * the vote metrics, and then sync the vote entities
     * from the database into memory.
     *
     * @param avaire The main AvaIre instance.
     */
    public VoteManager(AvaIre avaire) {
        this.avaire = avaire;

        for (VoteMetricType type : VoteMetricType.values()) {
            Metrics.dblVotes.labels(type.getName()).inc(0);
        }
        Metrics.validVotes.set(0D);

        if (isEnabled()) {
            syncWithDatabase();
            avaire.getServlet().registerPost("/vote", new PostVote());
        }
    }

    /**
     * Gets the vote messenger instance, the vote messenger is used to
     * sending vote specific messages, like when someone votes for
     * the bot, or when a user attempts to use a command that
     * they much vote for to unlock.
     *
     * @return The vote messenger instance.
     */
    public VoteMessenger getMessenger() {
        return messenger;
    }

    /**
     * Gets the vote log, the key is the ID of the user that the vote
     * cache entity was created for, and the value is the vote cache
     * entity itself, containing information about the user, and
     * when their vote expires.
     *
     * @return The vote cache entity vote log map.
     */
    public Map<Long, VoteCacheEntity> getVoteLog() {
        return voteLog;
    }

    /**
     * Gets the delayed vote entity queue, the queue will contain any vote
     * entity that still needs to be checked directly with the DBL API.
     *
     * @return The vote entity queue.
     */
    public DelayQueue<VoteEntity> getQueue() {
        return queue;
    }

    /**
     * Adds the given vote entity to the queue if it is not null, doesn't already exists
     * in the queue, or if the user haven't already voted in the last 12 hours, if the
     * vote entity passes all the checks, a new consume duration time will be set for
     * the entity, allowing the {@link DrainVoteQueueTask drain vote queue task} to
     * determine when a vote entity is ready to be sent to the API.
     *
     * @param entity The vote entity that should be added to the queue.
     * @return <code>True</code> if the vote entity was added to the queue, <code>False</code> otherwise.
     */
    public boolean queueEntity(@Nullable VoteEntity entity) {
        if (entity == null || queue.contains(entity) || hasVoted(entity.getUserId())) {
            return false;
        }

        long diff = 0;
        if (lastCheck > System.currentTimeMillis()) {
            diff = lastCheck - System.currentTimeMillis();
            entity.setDuration(diff + VoteEntity.defaultDuration);
        }
        lastCheck = System.currentTimeMillis() + diff + VoteEntity.defaultDuration;

        queue.put(entity);

        return true;
    }

    /**
     * Checks if the given member has voted in the last 12 hours.
     *
     * @param member The member instance that should be checked.
     * @return <code>True</code> if the member has voted in the last 12 hours.
     */
    public boolean hasVoted(@Nullable Member member) {
        return member != null && hasVoted(member.getUser().getIdLong());
    }

    /**
     * Checks if the given user has voted in the last 12 hours.
     *
     * @param user The user instance that should be checked.
     * @return <code>True</code> if the user has voted in the last 12 hours.
     */
    public boolean hasVoted(@Nullable User user) {
        return user != null && hasVoted(user.getIdLong());
    }

    /**
     * Checks if the user with the given ID has voted in the last 12 hours.
     *
     * @param userId The user ID that should be checked.
     * @return <code>True</code> if the user has voted in the last 12 hours.
     */
    public boolean hasVoted(long userId) {
        return !isEnabled() || voteLog.containsKey(userId) && voteLog.get(userId).getCarbon().isFuture();
    }

    /**
     * Gets the expiration date for the given members vote.
     *
     * @param member The member that the expiration date should be retrieved from.
     * @return Possibly-null, the expiration date for the given members vote.
     */
    @Nullable
    public Carbon getExpireTime(@Nullable Member member) {
        if (member == null) {
            return null;
        }
        return getExpireTime(member.getUser().getIdLong());
    }

    /**
     * Gets the expiration date for the given users vote.
     *
     * @param user The user that the expiration date should be retrieved from.
     * @return Possibly-null, the expiration date for the given users vote.
     */
    @Nullable
    public Carbon getExpireTime(@Nullable User user) {
        if (user == null) {
            return null;
        }
        return getExpireTime(user.getIdLong());
    }

    /**
     * Gets the expiration date for the user vote with the given ID.
     *
     * @param userId The ID of the user that the expiration date should be retrieved from.
     * @return Possibly-null, the expiration date for the given users vote.
     */
    @Nullable
    public Carbon getExpireTime(long userId) {
        if (!voteLog.containsKey(userId)) {
            return null;
        }
        return voteLog.get(userId).getCarbon().copy();
    }

    /**
     * Registers a new vote for the given member.
     *
     * @param member The member that the vote should be registered for.
     * @param points The amount of points to reward the user.
     */
    public void registerVoteFor(@Nullable Member member, int points) {
        if (member == null) {
            return;
        }
        registerVoteFor(member.getUser().getIdLong(), points);
    }

    /**
     * Registers a new vote for the given user.
     *
     * @param user   The user that the vote should be registered for.
     * @param points The amount of points to reward the user.
     */
    public void registerVoteFor(@Nullable User user, int points) {
        if (user == null) {
            return;
        }
        registerVoteFor(user.getIdLong(), points);
    }

    /**
     * Registers a new vote for the given user ID.
     *
     * @param userId The user ID that the vote should be registered for.
     * @param points The amount of points to reward the user.
     */
    public void registerVoteFor(long userId, int points) {
        if (!isEnabled()) {
            return;
        }

        points = Math.max(1, points);

        if (!voteLog.containsKey(userId)) {
            voteLog.put(userId, new VoteCacheEntity(
                userId, Carbon.now().addHours(12)
            ));
        }

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userId).take(1).get();

            int finalPoints = points;
            if (collection.isEmpty()) {
                avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("user_id", userId);
                        statement.set("expires_in", voteLog.get(userId).getCarbon().toDayDateTimeString());
                        statement.set("points", finalPoints);
                        statement.set("points_total", finalPoints);
                    });

                voteLog.get(userId).setVotePoints(1);

                return;
            }

            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .useAsync(true)
                .where("user_id", userId)
                .update(statement -> {
                    statement.set("expires_in", voteLog.get(userId).getCarbon().toDayDateTimeString());
                    statement.setRaw("points", "`points` + " + finalPoints);
                    statement.setRaw("points_total", "`points_total` + " + finalPoints);
                });

            VoteCacheEntity voteEntity = voteLog.get(userId);

            voteEntity.setVotePoints(collection.first().getInt("points", 1) + finalPoints);
            voteEntity.setOptIn(collection.first().getBoolean("opt_in", true));
        } catch (SQLException e) {
            log.error("An SQLException was thrown while updating user vote information: ", e);
        }
    }

    @Nullable
    public VoteCacheEntity getVoteEntity(@Nonnull User user) {
        if (voteLog.containsKey(user.getIdLong())) {
            return voteLog.get(user.getIdLong());
        }

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", user.getIdLong()).take(1).get();

            if (collection.isEmpty()) {
                return null;
            }

            DataRow row = collection.first();

            VoteCacheEntity voteCacheEntity = new VoteCacheEntity(
                row.getLong("user_id"),
                row.getInt("points", 0),
                row.getBoolean("opt_in", true),
                row.getTimestamp("expires_in")
            );

            voteLog.put(voteCacheEntity.getUserId(), voteCacheEntity);

            return voteCacheEntity;
        } catch (SQLException ignored) {
            return null;
        }
    }

    /**
     * Gets the vote entity for the given user, if no vote entity was
     * found, a new vote entity will be created for the given user.
     *
     * @param user The user that the vote cache entity should be retrieved for.
     * @return The vote cache entity belonging to the given user.
     */
    @Nonnull
    public VoteCacheEntity getVoteEntityWithFallback(User user) {
        VoteCacheEntity voteEntity = getVoteEntity(user);
        if (voteEntity != null) {
            return voteEntity;
        }

        voteEntity = new VoteCacheEntity(
            user.getIdLong(), 0, true, Carbon.now().subDay()
        );

        voteLog.put(user.getIdLong(), voteEntity);

        return voteEntity;
    }

    /**
     * Checks if the vote manager is enabled or not.
     *
     * @return <code>True</code> if the vote manager is enabled, <code>False</code> otherwise.
     */
    public boolean isEnabled() {
        return avaire.getConfig().getBoolean("vote-lock.enabled", false);
    }

    private void syncWithDatabase() {
        log.info("Syncing votes with the database...");
        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME).get();

            if (collection.isEmpty()) {
                return;
            }

            int size = voteLog.size();
            for (DataRow row : collection) {
                Carbon expiresIn = row.getTimestamp("expires_in");
                if (expiresIn == null || expiresIn.isPast()) {
                    continue;
                }
                voteLog.put(row.getLong("user_id"), new VoteCacheEntity(
                    row.getLong("user_id"),
                    row.getInt("points", 0),
                    row.getBoolean("opt_in", true),
                    expiresIn
                ));
            }

            log.info("Syncing complete! {} vote entries was found that has not expired yet and was added to the vote log!",
                voteLog.size() - size
            );
        } catch (SQLException e) {
            log.error("An SQLException was thrown while fetching user vote information: ", e);
        }
    }
}
