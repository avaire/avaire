package com.avairebot.contracts.handlers;

import com.avairebot.handlers.events.ApplicationShutdownEvent;
import com.avairebot.handlers.events.ModlogActionEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@SuppressWarnings("WeakerAccess")
public abstract class EventListener extends ListenerAdapter implements net.dv8tion.jda.core.hooks.EventListener {

    /**
     * The modlog actionable event will be called when a modlog action is
     * carried out on a server with the modlog feature enabled.
     *
     * @param event The modlog actionable event.
     */
    public void onModlogAction(ModlogActionEvent event) {
        //
    }

    /**
     * The application shutdown event, this event will be called just before
     * the application shuts down, the event will be invoked when a bot
     * administrator uses a shutdown, restart, or update command.
     *
     * @param event The application shutdown event.
     */
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
