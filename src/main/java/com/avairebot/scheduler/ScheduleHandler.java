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

    private static final Set<ScheduledFuture<?>> tasks = new HashSet<>();
    private static final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder()
        .setPriority(Thread.MAX_PRIORITY)
        .setNameFormat("job-schedule-%d")
        .build()
    );

    /**
     * Registers a job with the scheduler service, the job will define how often
     * it should run, and the schedule service will then periodically run the
     * job on a separate thread when it's time for it to run.
     *
     * @param job The job that should be registered with the scheduler service.
     */
    public static void registerJob(@Nonnull Job job) {
        tasks.add(schedulerService.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit()));
    }

    /**
     * Gets a set of scheduled future instances for jobs
     * that are registered to the scheduler service.
     *
     * @return A set of scheduled future instances for registered jobs.
     */
    public static Set<ScheduledFuture<?>> entrySet() {
        return tasks;
    }

    /**
     * Gets the scheduler execution service used to
     * register the jobs with.
     *
     * @return The scheduler service used for all the registered jobs.
     */
    public static ScheduledExecutorService getScheduler() {
        return schedulerService;
    }
}
