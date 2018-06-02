package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class IsDMMessageMiddelware extends Middleware {

    public IsDMMessageMiddelware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        return "**This command can only be used in DMs.**";
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        return !message.getChannelType().isGuild() && stack.next();
    }
}
