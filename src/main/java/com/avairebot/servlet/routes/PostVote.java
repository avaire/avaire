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

package com.avairebot.servlet.routes;

import com.avairebot.AvaIre;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import com.avairebot.time.Carbon;
import com.avairebot.vote.VoteCacheEntity;
import com.avairebot.vote.VoteMetricType;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class PostVote extends SparkRoute {

    private static final Logger log = LoggerFactory.getLogger(PostVote.class);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info("Vote route has been hit by {} with the body: {}",
            request.ip(), request.body()
        );

        if (!hasValidAuthorizationHeader(request)) {
            log.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        VoteRequest voteRequest = AvaIre.gson.fromJson(request.body(), VoteRequest.class);

        if (!isValidVoteRequest(voteRequest)) {
            log.warn("Bad request, invalid JSON data given to justify a upvote request.");
            return buildResponse(response, 400, "Bad request, invalid JSON data given to justify a upvote request.");
        }

        User userById = AvaIre.getInstance().getShardManager().getUserById(voteRequest.user);
        if (userById == null || userById.isBot()) {
            log.warn("Invalid user ID given, the user is not on any server the bot is on.");
            return buildResponse(response, 404, "Invalid user ID given, the user is not on any server the bot is on.");
        }

        VoteCacheEntity voteEntity = AvaIre.getInstance().getVoteManager().getVoteEntityWithFallback(userById);
        voteEntity.setCarbon(Carbon.now().addHours(12));
        AvaIre.getInstance().getVoteManager().registerVoteFor(userById, voteRequest.isWeekend ? 2 : 1);

        Metrics.dblVotes.labels(VoteMetricType.WEBHOOK.getName()).inc();

        log.info("Vote has been registered by {} ({})",
            userById.getName() + "#" + userById.getDiscriminator(), userById.getId()
        );

        if (!voteEntity.isOptIn()) {
            return buildResponse(response, 200, "Vote registered, thanks for voting!");
        }

        AvaIre.getInstance().getVoteManager().getMessenger().SendThanksForVotingMessageInDM(userById, voteEntity.getVotePoints());

        return buildResponse(response, 200, "Vote registered, thanks for voting!");
    }

    private boolean isValidVoteRequest(VoteRequest request) {
        if (request == null) {
            return false;
        }

        if (request.bot == null || request.user == null || request.type == null) {
            return false;
        }

        if (!request.bot.equals(AvaIre.getInstance().getSelfUser().getId())) {
            return false;
        }

        return request.type.equalsIgnoreCase("upvote");
    }

    private class VoteRequest {

        private String bot;
        private String user;
        private String type;
        private boolean isWeekend;
    }
}
