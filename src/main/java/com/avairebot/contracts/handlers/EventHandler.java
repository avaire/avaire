package com.avairebot.contracts.handlers;

import com.avairebot.AvaIre;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class EventHandler extends ListenerAdapter {

    /**
     * The AvaIre class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final AvaIre avaire;

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public EventHandler(AvaIre avaire) {
        this.avaire = avaire;
    }
}
