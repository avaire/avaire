package com.avairebot.commands;

import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandMessage {

    public final Guild guild;
    public final Member member;
    public final TextChannel channel;
    public final Message message;

    private final boolean mentionableCommand;
    private final String aliasArguments;

    public CommandMessage(Message message) {
        this(message, false, new String[0]);
    }

    public CommandMessage(Message message, boolean mentionableCommand, String[] aliasArguments) {
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
        return member.getUser();
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
}
