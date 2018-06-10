package com.avairebot.scheduler;

import com.avairebot.contracts.scheduler.Job;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ScheduleHandler {

    private static final Set<ScheduledFuture<?>> TASKS = new HashSet<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder()
        .setPriority(Thread.MAX_PRIORITY)
        .setNameFormat("job-schedule-%d")
        .build()
    );

    public static void registerJob(@Nonnull Job job) {
        TASKS.add(SCHEDULER.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit()));
    }

    public static Set<ScheduledFuture<?>> entrySet() {
        return TASKS;
    }

    public static ScheduledExecutorService getScheduler() {
        return SCHEDULER;
    }
}
