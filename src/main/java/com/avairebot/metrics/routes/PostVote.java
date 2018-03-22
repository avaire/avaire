package com.avairebot.metrics.routes;

import com.avairebot.AvaIre;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.awt.*;

public class PostVote extends SparkRoute {

    public PostVote(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!hasValidAuthorizationHeader(request)) {
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        VoteRequest voteRequest = AvaIre.GSON.fromJson(request.body(), VoteRequest.class);

        if (!isValidVoteRequest(voteRequest)) {
            return buildResponse(response, 400, "Bad request, invalid JSON data given to justify a upvote request.");
        }

        User userById = metrics.getAvaire().getShardManager().getUserById(voteRequest.user);
        if (userById == null || userById.isBot()) {
            return buildResponse(response, 404, "Invalid user ID given, the user is not on any server the bot is on.");
        }

        metrics.getAvaire().getVoteManager().registerVoteFor(userById);

        AvaIre.getLogger().info(String.format("Vote has been registered by %s (%s)",
            userById.getName() + "#" + userById.getDiscriminator(), userById.getId()
        ));

        userById.openPrivateChannel().queue(message -> {
            message.sendMessage(
                MessageFactory.createEmbeddedBuilder()
                    .setColor(Color.decode("#E91E63"))
                    .setTitle("Thanks for voting!", "https://discordbots.org/bot/avaire")
                    .setDescription(
                        "Thanks for voting for [AvaIre](https://discordbots.org/bot/avaire)! It's really appreciated ❤"
                            + "\nRewards for voting is coming soon! <a:lurk:425394751357845506>")
                    .build()
            ).queue();
        });

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

    private class VoteRequest {
        private String bot;
        private String user;
        private String type;
    }
}
