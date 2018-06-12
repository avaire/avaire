package com.avairebot.vote;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

public class VoteManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteManager.class);
    private static final DelayQueue<VoteEntity> queue = new DelayQueue<>();
    private static final Map<Long, Carbon> voteLog = new HashMap<>();
    private static final VoteMesseger messager = new VoteMesseger();
    private static long lastCheck = -1;

    private final AvaIre avaire;

    public VoteManager(AvaIre avaire) {
        this.avaire = avaire;

        this.syncWithDatabase();
    }

    public VoteMesseger getMessager() {
        return messager;
    }

    public DelayQueue<VoteEntity> getQueue() {
        return queue;
    }

    public boolean queueEntity(VoteEntity entity) {
        if (entity == null || queue.contains(entity) || hasVoted(entity.getUserId())) {
            return false;
        }

        long diff = 0;
        if (lastCheck > System.currentTimeMillis()) {
            diff = lastCheck - System.currentTimeMillis();
            entity.setDuration(diff + VoteEntity.DEFAULT_DURATION);
        }
        lastCheck = System.currentTimeMillis() + diff + VoteEntity.DEFAULT_DURATION;

        queue.put(entity);

        return true;
    }

    public boolean hasVoted(Member member) {
        return member != null && hasVoted(member.getUser().getIdLong());
    }

    public boolean hasVoted(User user) {
        return user != null && hasVoted(user.getIdLong());
    }

    public boolean hasVoted(long userId) {
        return !isEnabled() || voteLog.containsKey(userId) && voteLog.get(userId).isFuture();
    }

    public Carbon getExpireTime(Member member) {
        if (member == null) {
            return null;
        }
        return getExpireTime(member.getUser().getIdLong());
    }

    public Carbon getExpireTime(User user) {
        if (user == null) {
            return null;
        }
        return getExpireTime(user.getIdLong());
    }

    public Carbon getExpireTime(long userId) {
        if (!voteLog.containsKey(userId)) {
            return null;
        }
        return voteLog.get(userId).copy();
    }

    public void registerVoteFor(Member member) {
        if (member == null) {
            return;
        }
        registerVoteFor(member.getUser().getIdLong());
    }

    public void registerVoteFor(User user) {
        if (user == null) {
            return;
        }
        registerVoteFor(user.getIdLong());
    }

    public void registerVoteFor(long userId) {
        if (!isEnabled()) {
            return;
        }

        voteLog.put(userId, Carbon.now().addHours(24));

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userId).take(1).get();

            if (collection.isEmpty()) {
                avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("user_id", userId);
                        statement.set("expires_in", voteLog.get(userId).toDayDateTimeString());
                        statement.set("points", 1);
                        statement.set("points_total", 1);
                    });

                return;
            }

            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .useAsync(true)
                .where("user_id", userId)
                .update(statement -> {
                    statement.set("expires_in", voteLog.get(userId).toDayDateTimeString());
                    statement.setRaw("points", "`points` + 1");
                    statement.setRaw("points_total", "`points_total` + 1");
                });
        } catch (SQLException e) {
            LOGGER.error("An SQLException was thrown while updating user vote information: ", e);
        }
    }

    public int getVotePoints(AvaIre avaire, User userById) {
        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userById.getIdLong()).take(1).get();

            if (collection.isEmpty()) {
                return 0;
            }

            if (collection.first().getInt("opt_in", 1) == 0) {
                return Integer.MIN_VALUE;
            }

            return collection.first().getInt("points", 0);
        } catch (SQLException ignored) {
            return 0;
        }
    }

    public boolean isEnabled() {
        return avaire.getConfig().getBoolean("vote-lock.enabled", false);
    }

    private void syncWithDatabase() {
        LOGGER.info("Syncing votes with the database...");
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
                voteLog.put(row.getLong("user_id"), expiresIn);
            }

            LOGGER.info("Syncing complete! {} vote entries was found that has not expired yet and was added to the vote log!",
                voteLog.size() - size
            );
        } catch (SQLException e) {
            LOGGER.error("An SQLException was thrown while fetching user vote information: ", e);
        }
    }
}
