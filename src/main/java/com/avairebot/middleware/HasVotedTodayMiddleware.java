package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

public class HasVotedTodayMiddleware extends Middleware {

    public HasVotedTodayMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(String[] arguments) {
        return "**You must [vote for Ava](https://discordbots.org/bot/avaire) to use this command**";
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (avaire.getVoteManager().isEnabled() && isServerVIP(message)) {
            return stack.next();
        }

        if (avaire.getVoteManager().hasVoted(message.getAuthor())) {
            return stack.next();
        }

        avaire.getVoteManager().sendMustVoteMessage(message.getChannel());

        return false;
    }

    private boolean isServerVIP(Message message) {
        if (!message.getChannelType().isGuild()) {
            return false;
        }

        GuildTransformer transformer = GuildController.fetchGuild(avaire, message.getGuild());
        return transformer != null && !transformer.getType().isDefault();
    }
}
