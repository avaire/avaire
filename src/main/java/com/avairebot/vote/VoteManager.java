package com.avairebot.vote;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.factories.MessageFactory;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class VoteManager {

    private static final Map<String, Carbon> voteLog = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteManager.class);
    private static final List<String> hasBeenChecked = new ArrayList<>();

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
                hasBeenChecked.add(userId);
                return false;
            }

            DataRow first = collection.first();
            Carbon expiresIn = first.getTimestamp("expires_in");

            if (expiresIn == null) {
                return false;
            }

            voteLog.put(userId, expiresIn);

            return expiresIn.isFuture();
        } catch (SQLException e) {
            LOGGER.error("An SQLException was thrown while fetching user vote information: ", e);
        }

        return false;
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
            hasBeenChecked.remove(hasBeenChecked.indexOf(userId));
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
                    });

                return;
            }

            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .where("user_id", userId)
                .update(statement -> statement.set("expires_in", voteLog.get(userId).toDayDateTimeString()));

        } catch (SQLException e) {
            LOGGER.error("An SQLException was thrown while updating user vote information: ", e);
        }
    }

    public boolean isEnabled() {
        return avaire.getConfig().getBoolean("vote-lock.enabled", false);
    }

    public void syncVotesWithAPI() {
        String apiToken = avaire.getConfig().getString("vote-lock.vote-sync-token");
        if (apiToken == null || apiToken.trim().length() == 0) {
            return;
        }

        hasBeenChecked.clear();

        if (apiToken.equalsIgnoreCase("ReplaceThisWithYourAPITokenForDBL")) {
            return;
        }

        RequestFactory.makeGET("https://discordbots.org/api/bots/275270122082533378/votes")
            .addParameter("days", 1)
            .addParameter("onlyids", true)
            .addHeader("Authorization", avaire.getConfig().getString("vote-lock.vote-sync-token"))
            .send((Consumer<Response>) this::accept);
    }

    private void accept(Response response) {
        if (response.getResponse().code() != 200) {
            return;
        }

        String expiresIn = new Carbon(response.getResponse().header("Date"))
            .addDay().startOfDay().toDayDateTimeString();

        Object obj = response.toService(List.class);

        if (!(obj instanceof List)) {
            return;
        }

        List<String> userVotes = (List<String>) obj;
        if (userVotes.isEmpty()) {
            return;
        }

        QueryBuilder builder = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME);

        for (String userId : userVotes) {
            builder.orWhere("user_id", userId);
        }

        try {
            Collection collection = builder.get();

            List<String> updateUsers = new ArrayList<>();
            Iterator<String> iterator = userVotes.iterator();
            while (iterator.hasNext()) {
                String userId = iterator.next();

                if (collection.contains(userId)) {
                    iterator.remove();
                    updateUsers.add(userId);
                }
            }

            if (!userVotes.isEmpty()) {
                for (String userId : userVotes) {
                    avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                        .insert(statement -> {
                            statement.set("user_id", userId);
                            statement.set("expires_in", expiresIn);
                        });
                }
            }

            if (!updateUsers.isEmpty()) {
                QueryBuilder updateBuilder = avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME);

                for (String userId : updateUsers) {
                    updateBuilder.orWhere("user_id", userId);
                }

                updateBuilder.update(statement -> statement.set("expires_in", expiresIn));
            }

            LOGGER.info("{} new voters and {} old voters has been synced to the database",
                userVotes.size(), updateUsers.size()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
