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

    public VoteManager(AvaIre avaire) {
        this.avaire = avaire;

        this.syncWithDatabase();
    }

    public VoteMessenger getMessenger() {
        return messenger;
    }

    public Map<Long, VoteCacheEntity> getVoteLog() {
        return voteLog;
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
            entity.setDuration(diff + VoteEntity.defaultDuration);
        }
        lastCheck = System.currentTimeMillis() + diff + VoteEntity.defaultDuration;

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
        return !isEnabled() || voteLog.containsKey(userId) && voteLog.get(userId).getCarbon().isFuture();
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
        return voteLog.get(userId).getCarbon().copy();
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

        if (!voteLog.containsKey(userId)) {
            voteLog.put(userId, new VoteCacheEntity(
                userId, Carbon.now().addHours(24)
            ));
        }

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userId).take(1).get();

            if (collection.isEmpty()) {
                avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("user_id", userId);
                        statement.set("expires_in", voteLog.get(userId).getCarbon().toDayDateTimeString());
                        statement.set("points", 1);
                        statement.set("points_total", 1);
                    });

                voteLog.get(userId).setVotePoints(1);

                return;
            }

            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .useAsync(true)
                .where("user_id", userId)
                .update(statement -> {
                    statement.set("expires_in", voteLog.get(userId).getCarbon().toDayDateTimeString());
                    statement.setRaw("points", "`points` + 1");
                    statement.setRaw("points_total", "`points_total` + 1");
                });

            VoteCacheEntity voteEntity = voteLog.get(userId);

            voteEntity.setVotePoints(collection.first().getInt("points", 1) + 1);
            voteEntity.setOptIn(collection.first().getBoolean("opt_in", true));
        } catch (SQLException e) {
            log.error("An SQLException was thrown while updating user vote information: ", e);
        }
    }

    @Nullable
    public VoteCacheEntity getVoteEntity(AvaIre avaire, User user) {
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

    @Nonnull
    public VoteCacheEntity getVoteEntityWithFallback(AvaIre avaire, User user) {
        VoteCacheEntity voteEntity = getVoteEntity(avaire, user);
        if (voteEntity != null) {
            return voteEntity;
        }

        voteEntity = new VoteCacheEntity(
            user.getIdLong(), 0, true, Carbon.now().subDay()
        );

        voteLog.put(user.getIdLong(), voteEntity);

        return voteEntity;
    }

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
