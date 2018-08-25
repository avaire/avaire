package com.avairebot.contracts.scheduler;

import com.avairebot.AvaIre;

public interface Task {

    /**
     * Handles the task when the task is ready to be invoked.
     *
     * @param avaire The AvaIre class instance.
     */
    void handle(AvaIre avaire);
}
