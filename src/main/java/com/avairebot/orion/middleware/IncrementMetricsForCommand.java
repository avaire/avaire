package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.Middleware;
import com.avairebot.orion.metrics.Metrics;
import net.dv8tion.jda.core.entities.Message;

public class IncrementMetricsForCommand extends Middleware {

    /**
     * Instantiates the middleware and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public IncrementMetricsForCommand(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        Metrics.commandsReceived.labels(stack.getCommand().getClass().getSimpleName()).inc();

        return stack.next();
    }
}
