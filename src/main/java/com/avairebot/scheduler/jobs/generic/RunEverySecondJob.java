package com.avairebot.scheduler.jobs.generic;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.scheduler.tasks.ApplicationShutdownTask;
import com.avairebot.scheduler.tasks.DrainVoteQueueTask;

import java.util.concurrent.TimeUnit;

public class RunEverySecondJob extends Job {

    private final DrainVoteQueueTask emptyVoteQueueTask = new DrainVoteQueueTask();
    private final ApplicationShutdownTask shutdownTask = new ApplicationShutdownTask();

    public RunEverySecondJob(AvaIre avaire) {
        super(avaire, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        handleTask(emptyVoteQueueTask, shutdownTask);
    }
}
