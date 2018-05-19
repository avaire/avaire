package com.avairebot.vote;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.factories.MessageFactory;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class VoteManager {

    private static final Map<String, Carbon> voteLog = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteManager.class);
    private static final Set<String> hasBeenChecked = new HashSet<>();
    private static int ratelimit = 0;

    private final AvaIre avaire;

    public VoteManager(AvaIre avaire) {
        this.avaire = avaire;
    }

    public boolean hasVoted(Member member) {
        if (member == null) {
            return false;
        }
        return hasVoted(member.getUser().getId());
    }

    public boolean hasVoted(User user) {
        if (user == null) {
            return false;
        }
        return hasVoted(user.getId());
    }

    public boolean hasVoted(@Nonnull String userId) {
        if (!isEnabled()) {
            return true;
        }

        if (voteLog.containsKey(userId)) {
            return voteLog.get(userId).isFuture();
        }

        if (hasBeenChecked.contains(userId)) {
            return false;
        }

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userId).take(1).get();

            if (collection.isEmpty()) {
                syncVoteForUserWithAPI(userId);
                return false;
            }

            DataRow first = collection.first();
            Carbon expiresIn = first.getTimestamp("expires_in");

            if (expiresIn == null) {
                hasBeenChecked.add(userId);
                return false;
            }

            voteLog.put(userId, expiresIn);

            return expiresIn.isFuture();
        } catch (SQLException e) {
            LOGGER.error("An SQLException was thrown while fetching user vote information: ", e);
        }

        return false;
    }

    private void syncVoteForUserWithAPI(final @Nonnull String userId) {
        String apiToken = avaire.getConfig().getString("vote-lock.vote-sync-token");
        if (apiToken == null || apiToken.trim().length() == 0) {
            return;
        }

        if (apiToken.equalsIgnoreCase("ReplaceThisWithYourAPITokenForDBL")) {
            return;
        }

        // DiscordBots rates limits their API for up to 60 requests per minute as of right
        // now, to prevent being blocked we're just checking if we have already preformed
        // enough requests in the last minute, if we have we'll prematurely return false.
        if (ratelimit++ > 60) {
            return;
        }

        LOGGER.info("No vote record was found for {}, checking the API...", userId);

        RequestFactory.makeGET("https://discordbots.org/api/bots/275270122082533378/check")
            .addParameter("userId", userId)
            .addHeader("Authorization", avaire.getConfig().getString("vote-lock.vote-sync-token"))
            .send((Consumer<Response>) response -> accept(response, userId));
    }

    private void accept(Response response, String userId) {
        if (response.getResponse().code() != 200) {
            hasBeenChecked.add(userId);
            return;
        }

        Object obj = response.toService(Map.class);
        if (!(obj instanceof Map)) {
            hasBeenChecked.add(userId);
            return;
        }

        Map<String, Object> data = (Map<String, Object>) obj;
        if (data.isEmpty()) {
            hasBeenChecked.add(userId);
            return;
        }

        if (NumberUtil.parseInt(data.getOrDefault("voted", "0.0").toString().split("\\.")[0]) != 1) {
            hasBeenChecked.add(userId);
            return;
        }

        Carbon expiresIn = new Carbon(response.getResponse().header("Date"))
            .addDay().startOfDay();

        voteLog.put(userId, expiresIn);
        LOGGER.info("Vote record for {} was found, registering vote that expires on {}", userId, expiresIn.toDateTimeString());

        try {
            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .insert(statement -> {
                    statement.set("user_id", userId);
                    statement.set("expires_in", expiresIn.toDayDateTimeString());
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerVoteFor(Member member) {
        if (member == null) {
            return;
        }
        registerVoteFor(member.getUser().getId());
    }

    public void registerVoteFor(User user) {
        if (user == null) {
            return;
        }
        registerVoteFor(user.getId());
    }

    public void registerVoteFor(String userId) {
        if (!isEnabled()) {
            return;
        }

        if (hasBeenChecked.contains(userId)) {
            hasBeenChecked.remove(userId);
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

    public void resetHasBeenCheckedSetAndRatelimit() {
        hasBeenChecked.clear();
        ratelimit = 0;
    }

    public boolean isEnabled() {
        return avaire.getConfig().getBoolean("vote-lock.enabled", false);
    }

    public void sendMustVoteMessage(MessageChannel channel) {
        sendMustVoteMessage(channel, null);
    }

    public void sendMustVoteMessage(MessageChannel channel, String feature) {
        channel.sendMessage(
            MessageFactory.createEmbeddedBuilder()
                .setColor(Color.decode("#E91E63"))
                .setTitle("Vote for AvaIre on DBL", "https://discordbots.org/bot/avaire")
                .setDescription(String.format(
                    "You must vote to %s, voting is free and only takes a few seconds\nTo get started, head over to:\n\nhttps://discordbots.org/bot/avaire\n\nOnce you've voted you'll gain access to this, and other commands for the next 24 hours!",
                    feature == null ? "use this command" : feature
                ))
                .build()
        ).queue();
    }
}
