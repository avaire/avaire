package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import net.dv8tion.jda.core.entities.Message;

public class HasVotedTodayMiddleware extends Middleware {

    public HasVotedTodayMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (avaire.getVoteManager().hasVoted(message.getAuthor())) {
            return stack.next();
        }

        avaire.getVoteManager().sendMustVoteMessage(message.getChannel());

        return false;
    }
}
