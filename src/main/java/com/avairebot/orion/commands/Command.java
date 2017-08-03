package com.avairebot.orion.commands;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class Command {
    protected final Orion orion;

    public Command(Orion orion) {
        this.orion = orion;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<String> getTriggers();

    public abstract void onCommand(MessageReceivedEvent event, String[] args);
}
