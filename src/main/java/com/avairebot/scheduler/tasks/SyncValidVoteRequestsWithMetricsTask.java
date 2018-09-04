package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.metrics.Metrics;
import com.avairebot.vote.VoteCacheEntity;

public class SyncValidVoteRequestsWithMetricsTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        int validVotes = 0;

        for (VoteCacheEntity cacheEntity : avaire.getVoteManager().getVoteLog().values()) {
            if (cacheEntity.getCarbon().isFuture()) {
                validVotes++;
            }
        }

        Metrics.validVotes.set(validVotes);
    }
}
