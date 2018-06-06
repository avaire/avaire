package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.commands.fun.RipCommand;
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
            RipCommand.RESPECT = 0;
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
