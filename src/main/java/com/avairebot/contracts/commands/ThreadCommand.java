package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ThreadCommand extends Command {

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(25);

    public ThreadCommand(AvaIre avaire, boolean allowDM) {
        super(avaire, allowDM);
    }

    public ThreadCommand(AvaIre avaire) {
        super(avaire);
    }

    public final void submitTask(Callable<Boolean> task) {
        SERVICE.submit(task);
    }
}
