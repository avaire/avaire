package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventListener;
import com.avairebot.plugin.PluginLoader;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

public class EventEmitter {

    private final AvaIre avaire;

    public EventEmitter(AvaIre avaire) {
        this.avaire = avaire;
    }

    public void push(Event event) {
        Checks.notNull(event, "event instance");
        for (PluginLoader plugin : avaire.getPluginManager().getPlugins()) {
            for (ListenerAdapter listener : plugin.getEventListeners()) {
                if (listener != null && listener instanceof EventListener) {
                    ((EventListener) listener).onCustomEvent(event);
                }
            }
        }
    }
}
