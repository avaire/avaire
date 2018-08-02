package com.avairebot.contracts.handlers;

import com.avairebot.handlers.events.ApplicationShutdownEvent;
import com.avairebot.handlers.events.ModlogActionEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@SuppressWarnings("WeakerAccess")
public abstract class EventListener extends ListenerAdapter implements net.dv8tion.jda.core.hooks.EventListener {

    public void onModlogAction(ModlogActionEvent event) {
        //
    }

    public void onApplicationShutdown(ApplicationShutdownEvent event) {
        //
    }

    public final void onCustomEvent(Event event) {
        onGenericEvent(event);

        if (event instanceof ModlogActionEvent) {
            onModlogAction((ModlogActionEvent) event);
        } else if (event instanceof ApplicationShutdownEvent) {
            onApplicationShutdown((ApplicationShutdownEvent) event);
        }
    }
}
