package com.avairebot.orion.contracts.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class AbstractMiddleware {

    protected final Orion orion;

    public AbstractMiddleware(Orion orion) {
        this.orion = orion;
    }

    public abstract void handle(MessageReceivedEvent event, MiddlewareStack stack, String... args);
}
