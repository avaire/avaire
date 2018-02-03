package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ThreadCommand extends Command {

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(4);

    public ThreadCommand(AvaIre avaire, boolean allowDM) {
        super(avaire, allowDM);
    }

    public ThreadCommand(AvaIre avaire) {
        super(avaire);
    }

    public final void runThreadCommand(CommandMessage message, String[] args) {
        SERVICE.submit(() -> onCommand(message, args));
    }
}
