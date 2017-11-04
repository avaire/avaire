package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.Middleware;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class IsBotAdmin extends Middleware {

    public IsBotAdmin(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!orion.getConfig().getBotAccess().contains(message.getAuthor().getId())) {
            MessageFactory.makeError(message, ":warning: You must be a bot administrator to use this command!").queue();
            return false;
        }

        return stack.next();
    }
}
