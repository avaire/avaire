package com.avairebot.orion.contracts.handlers;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class EventHandler extends ListenerAdapter {

    protected final Orion orion;

    public EventHandler(Orion orion) {
        this.orion = orion;
    }
}
