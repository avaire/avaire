/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands;

import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.DatabaseEventHolder;
import com.avairebot.language.I18n;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandMessage implements CommandContext {

    private static final Logger log = LoggerFactory.getLogger(CommandMessage.class);

    public final Guild guild;
    public final Member member;
    public final TextChannel channel;
    public final Message message;

    private final boolean mentionableCommand;
    private final String aliasArguments;
    private final DatabaseEventHolder databaseEventHolder;

    private YamlConfiguration i18n;
    private String i18nCommandPrefix;

    public CommandMessage(CommandContext message) {
        this(null, message.getDatabaseEventHolder(), message.getMessage(), false, new String[0]);
    }

    public CommandMessage(CommandContext message, DatabaseEventHolder databaseEventHolder) {
        this(null, databaseEventHolder, message.getMessage(), false, new String[0]);
    }

    public CommandMessage(CommandContainer container, DatabaseEventHolder databaseEventHolder, Message message) {
        this(container, databaseEventHolder, message, false, new String[0]);
    }

    public CommandMessage(CommandContainer container, DatabaseEventHolder databaseEventHolder, Message message, boolean mentionableCommand, String[] aliasArguments) {
        if (container != null) {
            setI18nCommandPrefix(container);
        }

        this.message = message;

        this.guild = message.getGuild();
        this.member = message.getMember();
        this.channel = message.getTextChannel();
        this.databaseEventHolder = databaseEventHolder;

        this.mentionableCommand = mentionableCommand;
        this.aliasArguments = aliasArguments.length == 0 ?
            null : String.join(" ", aliasArguments);
    }

    public boolean canDelete() {
        return isGuildMessage() && getGuild().getSelfMember().hasPermission(
            getChannel(), Permission.MESSAGE_MANAGE
        );
    }

    public AuditableRestAction<Void> delete() {
        return canDelete() ? message.delete() : new AuditableRestAction.EmptyRestAction<>(getJDA());
    }

    public JDA getJDA() {
        return message.getJDA();
    }

    public String getContentDisplay() {
        return parseContent(message.getContentDisplay());
    }

    public String getContentStripped() {
        return parseContent(message.getContentStripped());
    }

    public String getContentRaw() {
        String[] parts = message.getContentRaw().split(" ");

        return (aliasArguments == null ? "" : aliasArguments) + String.join(" ",
            Arrays.copyOfRange(parts, isMentionableCommand() ? 2 : 1, parts.length)
        );
    }

    private String parseContent(String content) {
        String[] parts = content.split(" ");

        if (!isMentionableCommand()) {
            return String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }

        int nameSize = (isGuildMessage() ?
            message.getGuild().getSelfMember().getEffectiveName() :
            message.getJDA().getSelfUser().getName()
        ).split(" ").length + 1;

        return String.join(" ", Arrays.copyOfRange(parts, nameSize, parts.length));
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public Member getMember() {
        return member;
    }

    @Override
    public User getAuthor() {
        return message.getAuthor();
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public MessageChannel getMessageChannel() {
        return message.getChannel();
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public GuildTransformer getGuildTransformer() {
        return databaseEventHolder == null ? null : databaseEventHolder.getGuild();
    }

    @Override
    public PlayerTransformer getPlayerTransformer() {
        return databaseEventHolder == null ? null : databaseEventHolder.getPlayer();
    }

    @Override
    public DatabaseEventHolder getDatabaseEventHolder() {
        return databaseEventHolder;
    }

    @Override
    public List<User> getMentionedUsers() {
        if (!isMentionableCommand()) {
            return message.getMentionedUsers();
        }

        List<User> mentions = new ArrayList<>(message.getMentionedUsers());
        if (!mentions.isEmpty()) {
            mentions.remove(0);
        }
        return mentions;
    }

    @Override
    public List<TextChannel> getMentionedChannels() {
        return message.getMentionedChannels();
    }

    @Override
    public boolean isMentionableCommand() {
        return mentionableCommand;
    }

    @Override
    public boolean mentionsEveryone() {
        return message.mentionsEveryone()
            || message.getContentRaw().contains("@everyone")
            || message.getContentRaw().contains("@here");
    }

    @Override
    public boolean isGuildMessage() {
        return message.getChannelType().isGuild();
    }

    @Override
    public boolean canTalk() {
        if (!isGuildMessage()) {
            return true;
        }

        return message.getGuild().getSelfMember().hasPermission(message.getTextChannel(),
            Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_EMBED_LINKS
        );
    }

    public PlaceholderMessage makeError(String message) {
        return MessageFactory.makeError(this.message, message);
    }

    public PlaceholderMessage makeWarning(String message) {
        return MessageFactory.makeWarning(this.message, message);
    }

    public PlaceholderMessage makeSuccess(String message) {
        return MessageFactory.makeSuccess(this.message, message);
    }

    public PlaceholderMessage makeInfo(String message) {
        return MessageFactory.makeInfo(this.message, message);
    }

    public PlaceholderMessage makeEmbeddedMessage(Color color, String message) {
        return MessageFactory.makeEmbeddedMessage(this.message, color, message);
    }

    public PlaceholderMessage makeEmbeddedMessage(MessageType type, MessageEmbed.Field... fields) {
        return makeEmbeddedMessage(type.getColor(), fields);
    }

    public PlaceholderMessage makeEmbeddedMessage(Color color, MessageEmbed.Field... fields) {
        return MessageFactory.makeEmbeddedMessage(this.message.getChannel(), color, fields);
    }

    public PlaceholderMessage makeEmbeddedMessage() {
        return MessageFactory.makeEmbeddedMessage(this.message.getChannel());
    }

    @Override
    @Nonnull
    public YamlConfiguration getI18n() {
        if (this.i18n == null) {
            this.i18n = I18n.get(getGuild());
        }
        return this.i18n;
    }

    public CommandMessage setI18n(YamlConfiguration i18n) {
        this.i18n = i18n;
        return this;
    }

    @Override
    @CheckReturnValue
    public String i18n(@Nonnull String key) {
        if (i18nCommandPrefix != null) {
            key = i18nCommandPrefix + "." + key;
        }
        return i18nRaw(key);
    }

    @Override
    public String i18n(@Nonnull String key, Object... args) {
        if (i18nCommandPrefix != null) {
            key = i18nCommandPrefix + "." + key;
        }
        return i18nRaw(key, args);
    }

    @Override
    @CheckReturnValue
    public String i18nRaw(@Nonnull String key) {
        if (getI18n().contains(key)) {
            return getI18n().getString(key)
                .replace("\\n", "\n")
                .replace("\\t", "\t");
        } else {
            log.warn("Missing language entry for key {} in language {}", key, I18n.getLocale(getGuild()).getLanguage().getCode());
            return I18n.getDefaultLanguage().getConfig().getString(key)
                .replace("\\n", "\n")
                .replace("\\t", "\t");
        }
    }

    @Override
    public String i18nRaw(@Nonnull String key, Object... args) {
        String message = i18nRaw(key);
        if (message == null) {
            return null;
        }
        return I18n.format(message, args);
    }

    @Override
    public void setI18nPrefix(@Nullable String i18nPrefix) {
        this.i18nCommandPrefix = i18nPrefix;
    }

    @Override
    public String getI18nCommandPrefix() {
        return i18nCommandPrefix;
    }

    @Override
    public void setI18nCommandPrefix(@Nonnull CommandContainer container) {
        setI18nPrefix(
            container.getCategory().getName().toLowerCase() + "."
                + container.getCommand().getClass().getSimpleName()
        );
    }
}
