package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.cache.CacheAdapter;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import com.avairebot.language.I18n;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class IsMusicChannelMiddleware extends Middleware {

    public IsMusicChannelMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild() || stack.getDatabaseEventHolder().getGuild() == null) {
            return stack.next();
        }

        if (stack.getDatabaseEventHolder().getGuild().getMusicChannelText() == null) {
            return stack.next();
        }

        TextChannel textChannelById = message.getGuild().getTextChannelById(
            stack.getDatabaseEventHolder().getGuild().getMusicChannelText()
        );

        if (textChannelById == null) {
            return stack.next();
        }

        if (message.getChannel().getIdLong() == textChannelById.getIdLong()) {
            return stack.next();
        }

        if (shouldSendMessage(textChannelById.getIdLong())) {
            MessageFactory.makeWarning(message, I18n.get(message.getGuild()).getString(
                "music.internal.musicChannel",
                "You can only use music commands in the :channel channel."
            )).set("channel", textChannelById.getAsMention()).queue(
                musicMessage -> musicMessage.delete().queueAfter(30, TimeUnit.SECONDS),
                RestActionUtil.ignore
            );
        }

        return false;
    }

    private boolean shouldSendMessage(long id) {
        if (getMemoryAdapter().has("music-channel." + id)) {
            return false;
        }

        getMemoryAdapter().put("music-channel." + id, 1, 30);
        return true;
    }

    private CacheAdapter getMemoryAdapter() {
        return avaire.getCache().getAdapter(CacheType.MEMORY);
    }
}
