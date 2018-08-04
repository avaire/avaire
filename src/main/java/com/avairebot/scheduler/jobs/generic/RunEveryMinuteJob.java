package com.avairebot.scheduler.jobs.generic;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.scheduler.tasks.*;

import java.util.concurrent.TimeUnit;

public class RunEveryMinuteJob extends Job {

    private final ChangeGameTask changeGameTask = new ChangeGameTask();
    private final GarbageCollectorTask garbageCollectorTask = new GarbageCollectorTask();
    private final ResetRespectStatisticsTask resetRespectStatisticsTask = new ResetRespectStatisticsTask();
    private final UpdateWebsocketHeartbeatMetricsTask updateWebsocketHeartbeatMetricsTask = new UpdateWebsocketHeartbeatMetricsTask();
    private final SyncPlayerExperienceWithDatabaseTask syncPlayerExperienceWithDatabaseTask = new SyncPlayerExperienceWithDatabaseTask();
    private final SyncPlayerUpdateReferencesWithDatabaseTask syncPlayerUpdateReferencesWithDatabaseTask = new SyncPlayerUpdateReferencesWithDatabaseTask();

    public RunEveryMinuteJob(AvaIre avaire) {
        super(avaire, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        handleTask(
            changeGameTask,
            garbageCollectorTask,
            resetRespectStatisticsTask,
            updateWebsocketHeartbeatMetricsTask,
            syncPlayerExperienceWithDatabaseTask,
            syncPlayerUpdateReferencesWithDatabaseTask
        );
    }
}
