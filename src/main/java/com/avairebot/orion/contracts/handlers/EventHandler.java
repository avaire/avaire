package com.avairebot.orion.contracts.handlers;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class EventHandler extends ListenerAdapter {

    /**
     * The Orion class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final Orion orion;

    /**
     * Instantiates the event handler and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public EventHandler(Orion orion) {
        this.orion = orion;
    }
}
