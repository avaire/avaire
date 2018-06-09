package com.avairebot.metrics.routes;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.database.collection.Collection;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.awt.*;
import java.sql.SQLException;

public class PostVote extends SparkRoute {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostVote.class);

    public PostVote(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        LOGGER.info("Vote route has been hit by {} with the body: {}",
            request.ip(), request.body()
        );

        if (!hasValidAuthorizationHeader(request)) {
            LOGGER.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        VoteRequest voteRequest = AvaIre.GSON.fromJson(request.body(), VoteRequest.class);

        if (!isValidVoteRequest(voteRequest)) {
            LOGGER.warn("Bad request, invalid JSON data given to justify a upvote request.");
            return buildResponse(response, 400, "Bad request, invalid JSON data given to justify a upvote request.");
        }

        User userById = metrics.getAvaire().getShardManager().getUserById(voteRequest.user);
        if (userById == null || userById.isBot()) {
            LOGGER.warn("Invalid user ID given, the user is not on any server the bot is on.");
            return buildResponse(response, 404, "Invalid user ID given, the user is not on any server the bot is on.");
        }

        int userPoints = getVotePoints(metrics.getAvaire(), userById);
        metrics.getAvaire().getVoteManager().registerVoteFor(userById);

        LOGGER.info("Vote has been registered by {} ({})",
            userById.getName() + "#" + userById.getDiscriminator(), userById.getId()
        );

        if (userPoints == Integer.MIN_VALUE) {
            return buildResponse(response, 200, "Vote registered, thanks for voting!");
        }

        userById.openPrivateChannel().queue(message -> {
            message.sendMessage(
                MessageFactory.createEmbeddedBuilder()
                    .setColor(Color.decode("#E91E63"))
                    .setTitle("Thanks for voting!", "https://discordbots.org/bot/avaire")
                    .setFooter("Don't want to receive messages when you vote? use \"!voteopt out\"", null)
                    .setDescription(String.format(
                        "Thanks for voting for [AvaIre](https://discordbots.org/bot/avaire)! It's really appreciated ❤"
                            + "\nYou now have **%s** vote points, rewards for vote points is coming soon! <a:lurk:425394751357845506>"
                            + "\nYou now also have access to the `!volume` and `!default-volume` commands for the next 24 hours on servers you have permission to run them on.",
                        (userPoints + 1)
                    ))
                    .build()
            ).queue(null, RestActionUtil.IGNORE);
        }, RestActionUtil.IGNORE);

        return buildResponse(response, 200, "Vote registered, thanks for voting!");
    }

    private boolean isValidVoteRequest(VoteRequest request) {
        if (request == null) {
            return false;
        }

        if (request.bot == null || request.user == null || request.type == null) {
            return false;
        }

        if (!request.bot.equals(metrics.getAvaire().getSelfUser().getId())) {
            return false;
        }

        return request.type.equalsIgnoreCase("upvote");
    }

    private boolean hasValidAuthorizationHeader(Request request) {
        String authorization = request.headers("Authorization");

        return authorization != null && authorization.equals(getAuthorizationToken());
    }

    private String getAuthorizationToken() {
        return metrics.getAvaire().getConfig().getString("metrics.authToken", "avaire-auth-token");
    }

    private JSONObject buildResponse(Response response, int code, String message) {
        response.status(code);

        JSONObject root = new JSONObject();

        root.put("status", code);
        root.put(code == 200 ? "message" : "reason", message);

        return root;
    }

    private int getVotePoints(AvaIre avaire, User userById) {
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

    private class VoteRequest {
        private String bot;
        private String user;
        private String type;
    }
}
