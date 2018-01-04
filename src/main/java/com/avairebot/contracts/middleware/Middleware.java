package com.avairebot.contracts.middleware;

import com.avairebot.AvaIre;
import com.avairebot.middleware.MiddlewareStack;
import net.dv8tion.jda.core.entities.Message;

public abstract class Middleware {

    /**
     * The AvaIre class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final AvaIre avaire;

    /**
     * Instantiates the middleware and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public Middleware(AvaIre avaire) {
        this.avaire = avaire;
    }

    /**
     * Invoked by the middleware stack, handles the middleware request message
     * event, on success the {@link MiddlewareStack#next()} method should be
     * called to call the next middleware in the chain, on failure the
     * method should return false.
     *
     * @param message The JDA message object.
     * @param stack   The middleware stack for the current command.
     * @param args    The arguments given the current middleware.
     * @return Invoke {@link MiddlewareStack#next()} on success, false on failure.
     */
    public abstract boolean handle(Message message, MiddlewareStack stack, String... args);
}
