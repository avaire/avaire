package com.avairebot.contracts.handlers;

import com.avairebot.AvaIre;

public abstract class EventHandler extends EventListener {

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
