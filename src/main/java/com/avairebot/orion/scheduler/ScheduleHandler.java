package com.avairebot.orion.scheduler;

import com.avairebot.orion.contracts.scheduler.Job;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ScheduleHandler {

    private static final Map<String, ScheduledFuture<?>> TASKS = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public static String registerJob(@Nonnull Job job) {
        TASKS.put(job.getUniqueId(), SCHEDULER.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit()));
        return job.getUniqueId();
    }

    public static ScheduledFuture<?> getScheduledFuture(String jobId) {
        return TASKS.getOrDefault(jobId, null);
    }

    public static Set<Map.Entry<String, ScheduledFuture<?>>> entrySet() {
        return TASKS.entrySet();
    }
}
