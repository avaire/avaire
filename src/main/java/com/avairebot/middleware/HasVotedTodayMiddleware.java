package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class HasVotedTodayMiddleware extends Middleware {

    public HasVotedTodayMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        return "**You must [vote for Ava](https://discordbots.org/bot/avaire) to use this command**";
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (avaire.getVoteManager().isEnabled() && isServerVIP(stack, message)) {
            return stack.next();
        }

        if (avaire.getVoteManager().hasVoted(message.getAuthor())) {
            return stack.next();
        }

        avaire.getVoteManager().getMessenger().sendMustVoteMessage(message.getChannel());

        return false;
    }

    private boolean isServerVIP(MiddlewareStack stack, Message message) {
        if (!message.getChannelType().isGuild()) {
            return false;
        }

        GuildTransformer transformer = stack.getDatabaseEventHolder().getGuild();
        return transformer != null && !transformer.getType().isDefault();
    }
}
