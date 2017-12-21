package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.metrics.Metrics;
import net.dv8tion.jda.core.events.Event;

public class GenericEvent extends EventHandler {

    /**
     * Instantiates the event handler and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public GenericEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onGenericEvent(Event event) {
        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }
}
