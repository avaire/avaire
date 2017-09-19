package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.contracts.scheduler.Job;
import com.avairebot.orion.time.Carbon;

public class ResetRespectStatisticsJob extends Job {

    private int currentDay = 0;

    public ResetRespectStatisticsJob(Orion orion) {
        super(orion);
    }

    @Override
    public void run() {
        if (!isSameDay()) {
            Statistics.resetRespects();
        }
    }

    private boolean isSameDay() {
        if (Carbon.now().getDayOfYear() == currentDay) {
            return true;
        }

        currentDay = Carbon.now().getDayOfYear();
        return false;
    }
}
