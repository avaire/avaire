package com.avairebot.orion.scheduler;

import com.avairebot.orion.contracts.scheduler.Job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduleHandler {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public static void registerJob(Job job) {
        SCHEDULER.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit());
    }
}
