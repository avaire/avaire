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

    public static void registerJob(@Nonnull Job job) {
        tasks.add(schedulerService.scheduleAtFixedRate(job, job.getDelay(), job.getPeriod(), job.getUnit()));
    }

    public static Set<ScheduledFuture<?>> entrySet() {
        return tasks;
    }

    public static ScheduledExecutorService getScheduler() {
        return schedulerService;
    }
}
