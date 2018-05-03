package com.avairebot.contracts.handlers;

import com.avairebot.handlers.events.ModlogActionEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class EventListener extends ListenerAdapter {

    public void onModlogAction(ModlogActionEvent event) {
        //
    }

    public final void onCustomEvent(Event event) {
        onGenericEvent(event);
        if (event instanceof ModlogActionEvent) {
            onModlogAction((ModlogActionEvent) event);
        }
    }
}
