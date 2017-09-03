package com.avairebot.orion.contracts.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.entities.Message;

public abstract class AbstractMiddleware {

    protected final Orion orion;

    public AbstractMiddleware(Orion orion) {
        this.orion = orion;
    }

    public abstract boolean handle(Message message, MiddlewareStack stack, String... args);
}
