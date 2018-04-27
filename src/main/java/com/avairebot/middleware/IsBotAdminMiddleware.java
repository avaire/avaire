package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class IsBotAdminMiddleware extends Middleware {

    public IsBotAdminMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(String[] arguments) {
        return "**You must be a Bot Administrator to use this command!**";
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!avaire.getConfig().getStringList("botAccess").contains(message.getAuthor().getId())) {
            MessageFactory.makeError(message, ":warning: You must be a bot administrator to use this command!").queue();
            return false;
        }

        return stack.next();
    }
}
