package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.events.Event;

public class GenericEventHandler extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GenericEventHandler(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onGenericEvent(Event event) {
        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }
}
