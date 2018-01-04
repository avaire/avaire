package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.entities.Message;

public class IncrementMetricsForCommand extends Middleware {

    /**
     * Instantiates the middleware and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public IncrementMetricsForCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        Metrics.commandsReceived.labels(stack.getCommand().getClass().getSimpleName()).inc();

        return stack.next();
    }
}
