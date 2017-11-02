package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.entities.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ThreadCommand extends Command {

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(4);

    public ThreadCommand(Orion orion, boolean allowDM) {
        super(orion, allowDM);
    }

    public ThreadCommand(Orion orion) {
        super(orion);
    }

    public final void runThreadCommand(Message message, String[] args) {
        SERVICE.submit(() -> onCommand(message, args));
    }
}
