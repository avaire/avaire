package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class IsBotAdmin extends Middleware {

    public IsBotAdmin(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!avaire.getConfig().getBotAccess().contains(message.getAuthor().getId())) {
            MessageFactory.makeError(message, ":warning: You must be a bot administrator to use this command!").queue();
            return false;
        }

        return stack.next();
    }
}
