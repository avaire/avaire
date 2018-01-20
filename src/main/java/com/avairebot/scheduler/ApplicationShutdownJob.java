package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;

import java.util.concurrent.TimeUnit;

public class ApplicationShutdownJob extends Job {

    public ApplicationShutdownJob(AvaIre avaire) {
        super(avaire, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (avaire.getShutdownTime() == null || avaire.getShutdownTime().isFuture()) {
            return;
        }
        avaire.shutdown(avaire.getShutdownCode());
    }
}
