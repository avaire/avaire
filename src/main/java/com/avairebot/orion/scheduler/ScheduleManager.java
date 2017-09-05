package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.scheduler.Job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduleManager {

    private final Orion orion;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ScheduleManager(Orion orion) {
        this.orion = orion;
    }

    public void registerJob(Job job) {
        scheduler.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit());
    }
}
