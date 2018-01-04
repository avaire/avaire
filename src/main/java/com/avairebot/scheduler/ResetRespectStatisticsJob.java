package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.Statistics;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.time.Carbon;

public class ResetRespectStatisticsJob extends Job {

    private int currentDay = 0;

    public ResetRespectStatisticsJob(AvaIre avaire) {
        super(avaire);
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
