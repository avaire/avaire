package com.avairebot.commands;

import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.factories.MessageFactory;
import com.avairebot.language.I18n;
import net.dv8tion.jda.core.JDA;
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

public class CommandMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandMessage.class);

    public final Guild guild;
    public final Member member;
    public final TextChannel channel;
    public final Message message;

    private final boolean mentionableCommand;
    private final String aliasArguments;

    private YamlConfiguration i18n;
    private String i18nCommandPrefix;

    public CommandMessage(Message message) {
        this(null, message, false, new String[0]);
    }

    public CommandMessage(CommandContainer container, Message message) {
        this(container, message, false, new String[0]);
    }

    public CommandMessage(CommandContainer container, Message message, boolean mentionableCommand, String[] aliasArguments) {
        if (container != null) {
            setI18nCommandPrefix(container);
        }

        this.message = message;

        this.guild = message.getGuild();
        this.member = message.getMember();
        this.channel = message.getTextChannel();

        this.mentionableCommand = mentionableCommand;
        this.aliasArguments = aliasArguments.length == 0 ?
            null : String.join(" ", aliasArguments);
    }

    public AuditableRestAction<Void> delete() {
        return message.delete();
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

    public Guild getGuild() {
        return guild;
    }

    public Member getMember() {
        return member;
    }

    public User getAuthor() {
        return message.getAuthor();
    }

    public TextChannel getChannel() {
        return channel;
    }

    public MessageChannel getMessageChannel() {
        return message.getChannel();
    }

    public Message getMessage() {
        return message;
    }

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

    public List<TextChannel> getMentionedChannels() {
        return message.getMentionedChannels();
    }

    public boolean isMentionableCommand() {
        return mentionableCommand;
    }

    public boolean isGuildMessage() {
        return message.getChannelType().isGuild();
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

    @Nonnull
    public YamlConfiguration getI18n() {
        if (this.i18n == null) {
            this.i18n = I18n.get(getGuild());
        }
        return this.i18n;
    }

    @CheckReturnValue
    public String i18n(@Nonnull String key) {
        if (i18nCommandPrefix != null) {
            key = i18nCommandPrefix + "." + key;
        }
        return i18nRaw(key);
    }

    @CheckReturnValue
    public String i18nRaw(@Nonnull String key) {
        if (getI18n().contains(key)) {
            return getI18n().getString(key).replace("\\n", "\n");
        } else {
            LOGGER.warn("Missing language entry for key {} in language {}", key, I18n.getLocale(getGuild()).getCode());
            return I18n.DEFAULT.getConfig().getString(key).replace("\\n", "\n");
        }
    }

    public void setI18nPrefix(@Nullable String i18nPrefix) {
        this.i18nCommandPrefix = i18nPrefix;
    }

    public String getI18nCommandPrefix() {
        return i18nCommandPrefix;
    }

    public void setI18nCommandPrefix(@Nonnull CommandContainer container) {
        setI18nPrefix(
            container.getCategory().getName().toLowerCase() + "."
                + container.getCommand().getClass().getSimpleName()
        );
    }
}
