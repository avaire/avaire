package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;

public class ApplicationShutdownTask implements Task {

    @Override
    public void handle(AvaIre avaire) {
        if (avaire.getShutdownTime() == null || avaire.getShutdownTime().isFuture()) {
            return;
        }
        avaire.shutdown(avaire.getShutdownCode());
    }
}
