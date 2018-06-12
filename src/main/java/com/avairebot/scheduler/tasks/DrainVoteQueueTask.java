package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.vote.VoteEntity;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class DrainVoteQueueTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrainVoteQueueTask.class);

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

        LOGGER.info("Checking vote requests for {} with the DBL API...", entity.getUserId());

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

        LOGGER.info("Vote record for {} was found, registering vote that expires on {}", entity.getUserId(), expiresIn.toDateTimeString());

        User user = avaire.getShardManager().getUserById(entity.getUserId());
        if (user == null) {
            return;
        }

        int points = avaire.getVoteManager().getVotePoints(avaire, user);
        avaire.getVoteManager().registerVoteFor(user);

        LOGGER.info("Vote has been registered by {} ({})",
            user.getName() + "#" + user.getDiscriminator(), user.getId()
        );

        TextChannel textChannel = avaire.getShardManager().getTextChannelById(entity.getChannelId());
        if (textChannel == null || !textChannel.canTalk()) {
            if (points != Integer.MIN_VALUE) {
                avaire.getVoteManager().getMessager()
                    .sendVoteWithPointsMessageInDM(user, points);
            }
            return;
        }

        textChannel.sendMessage(
            avaire.getVoteManager().getMessager().buildVoteWithPointsMessage(
                "Your vote has been registered!", points
            )
        ).queue(null, error -> {
            if (points != Integer.MIN_VALUE) {
                avaire.getVoteManager().getMessager()
                    .sendVoteWithPointsMessageInDM(user, points);
            }
        });

    }
}
