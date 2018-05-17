package com.avairebot.contracts.commands;

import com.avairebot.commands.CommandContainer;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.handlers.DatabaseEventHolder;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CommandContext {

    Guild getGuild();

    Member getMember();

    User getAuthor();

    TextChannel getChannel();

    MessageChannel getMessageChannel();

    Message getMessage();

    @Nullable
    GuildTransformer getGuildTransformer();

    @Nullable
    PlayerTransformer getPlayerTransformer();

    @Nullable
    DatabaseEventHolder getDatabaseEventHolder();

    List<User> getMentionedUsers();

    List<TextChannel> getMentionedChannels();

    boolean isMentionableCommand();

    boolean isGuildMessage();

    @Nonnull
    YamlConfiguration getI18n();

    @CheckReturnValue
    String i18n(@Nonnull String key);

    @CheckReturnValue
    String i18nRaw(@Nonnull String key);

    void setI18nPrefix(@Nullable String i18nPrefix);

    String getI18nCommandPrefix();

    void setI18nCommandPrefix(@Nonnull CommandContainer container);
}
