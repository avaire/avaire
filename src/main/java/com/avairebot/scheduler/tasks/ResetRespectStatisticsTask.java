package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.commands.fun.RipCommand;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.time.Carbon;

public class ResetRespectStatisticsTask implements Task {

    private int currentDay = 0;

    @Override
    public void handle(AvaIre avaire) {
        if (!isSameDay()) {
            RipCommand.respect = 0;
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
