package com.avairebot.orion.contracts.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.entities.Message;

public abstract class AbstractMiddleware {

    /**
     * The Orion class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final Orion orion;

    /**
     * Instantiates the middleware and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public AbstractMiddleware(Orion orion) {
        this.orion = orion;
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
