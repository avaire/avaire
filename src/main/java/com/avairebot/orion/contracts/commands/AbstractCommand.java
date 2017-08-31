package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    protected final Orion orion;

    public AbstractCommand(Orion orion) {
        this.orion = orion;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<String> getTriggers();

    public List<String> getMiddleware() {
        return new ArrayList<>();
    }

    public abstract void onCommand(MessageReceivedEvent event, String[] args);
}
