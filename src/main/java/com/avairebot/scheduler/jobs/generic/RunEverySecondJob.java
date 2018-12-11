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

package com.avairebot.scheduler.jobs.generic;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.scheduler.tasks.ApplicationShutdownTask;
import com.avairebot.scheduler.tasks.DrainReactionRoleQueueTask;
import com.avairebot.scheduler.tasks.DrainVoteQueueTask;
import com.avairebot.scheduler.tasks.DrainWeatherQueueTask;

import java.util.concurrent.TimeUnit;

public class RunEverySecondJob extends Job {

    private final DrainVoteQueueTask emptyVoteQueueTask = new DrainVoteQueueTask();
    private final ApplicationShutdownTask shutdownTask = new ApplicationShutdownTask();
    private final DrainWeatherQueueTask drainWeatherQueueTask = new DrainWeatherQueueTask();
    private final DrainReactionRoleQueueTask reactionRoleQueueTask = new DrainReactionRoleQueueTask();

    public RunEverySecondJob(AvaIre avaire) {
        super(avaire, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        handleTask(emptyVoteQueueTask, shutdownTask, drainWeatherQueueTask, reactionRoleQueueTask);
    }
}
