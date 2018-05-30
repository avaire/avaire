package com.avairebot.middleware.global;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.metrics.Metrics;
import com.avairebot.middleware.MiddlewareStack;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class IncrementMetricsForCommand extends Middleware {

    public IncrementMetricsForCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        Metrics.commandsReceived.labels(stack.getCommand().getClass().getSimpleName()).inc();

        return stack.next();
    }
}
