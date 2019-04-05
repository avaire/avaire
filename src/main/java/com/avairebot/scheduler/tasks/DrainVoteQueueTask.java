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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.factories.RequestFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.vote.VoteCacheEntity;
import com.avairebot.vote.VoteEntity;
import com.avairebot.vote.VoteMetricType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class DrainVoteQueueTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(DrainVoteQueueTask.class);

    @Override
    public void handle(AvaIre avaire) {
        if (avaire.getVoteManager() == null || avaire.getVoteManager().getQueue().isEmpty()) {
            return;
        }

        VoteEntity entity = avaire.getVoteManager().getQueue().poll();
        if (entity == null) {
            return;
        }

        String apiToken = avaire.getConfig().getString("vote-lock.vote-sync-token");
        if (apiToken == null || apiToken.trim().length() == 0) {
            return;
        }

        if (apiToken.equalsIgnoreCase("ReplaceThisWithYourAPITokenForDBL")) {
            return;
        }

        log.info("Checking vote requests for {} with the DBL API...", entity.getUserId());

        RequestFactory.makeGET("https://discordbots.org/api/bots/275270122082533378/check")
            .addParameter("userId", entity.getUserId())
            .addHeader("Authorization", avaire.getConfig().getString("vote-lock.vote-sync-token"))
            .send((Consumer<Response>) response -> accept(avaire, response, entity));
    }

    private void accept(AvaIre avaire, Response response, VoteEntity entity) {
        if (response.getResponse().code() != 200) {
            return;
        }

        Object obj = response.toService(Map.class);
        if (!(obj instanceof Map)) {
            return;
        }

        Map<String, Object> data = (Map<String, Object>) obj;
        if (data.isEmpty()) {
            return;
        }

        int voted = NumberUtil.parseInt(data.getOrDefault("voted", "0.0")
            .toString().split("\\.")[0]);

        if (voted != 1) {
            return;
        }

        Carbon expiresIn = new Carbon(response.getResponse().header("Date")).addDay();

        Metrics.dblVotes.labels(VoteMetricType.COMMAND.getName()).inc();

        log.info("Vote record for {} was found, registering vote that expires on {}", entity.getUserId(), expiresIn.toDateTimeString());

        User user = avaire.getShardManager().getUserById(entity.getUserId());
        if (user == null) {
            return;
        }

        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntityWithFallback(user);
        voteEntity.setCarbon(expiresIn);

        avaire.getVoteManager().registerVoteFor(user.getIdLong(), 1);

        log.info("Vote has been registered by {} ({})",
            user.getName() + "#" + user.getDiscriminator(), user.getId()
        );

        TextChannel textChannel = avaire.getShardManager().getTextChannelById(entity.getChannelId());
        if (textChannel == null || !textChannel.canTalk()) {
            if (voteEntity.isOptIn()) {
                avaire.getVoteManager().getMessenger()
                    .SendThanksForVotingMessageInDM(user, voteEntity.getVotePoints());
            }
            return;
        }

        textChannel.sendMessage(
            avaire.getVoteManager().getMessenger().buildThanksForVotingMessage(
                "Your vote has been registered!", voteEntity.getVotePoints()
            )
        ).queue(null, error -> {
            if (voteEntity.isOptIn()) {
                avaire.getVoteManager().getMessenger()
                    .SendThanksForVotingMessageInDM(user, voteEntity.getVotePoints());
            }
        });
    }
}
