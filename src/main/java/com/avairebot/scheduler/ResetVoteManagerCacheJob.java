package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;

import java.util.concurrent.TimeUnit;

public class ResetVoteManagerCacheJob extends Job {

    public ResetVoteManagerCacheJob(AvaIre avaire) {
        super(avaire, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        avaire.getVoteManager().resetHasBeenCheckedSetAndRatelimit();
    }
}
