package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class IsBotAdmin extends AbstractMiddleware {

    public IsBotAdmin(Orion orion) {
        super(orion);
    }

    @Override
    public void handle(MessageReceivedEvent event, MiddlewareStack stack, String... args) {
        if (!orion.config.getBotAccess().contains(event.getAuthor().getId())) {
            MessageFactory.makeError(event.getMessage(), ":warning: You must be a bot administrator to use this command!").queue();
            return;
        }

        stack.next();
    }
}
