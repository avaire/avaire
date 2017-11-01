package com.avairebot.orion.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.MessageImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMessage extends MessageImpl {

    private final boolean mentionableCommand;

    public CommandMessage(Message message, boolean mentionableCommand, String[] aliasArguments) {
        super(message.getIdLong(), message.getChannel(), message.isWebhookMessage(), message.getType());

        this.mentionableCommand = mentionableCommand;
        String rawContent = prepareRawContent(message.getRawContent(), aliasArguments != null);
        if (aliasArguments != null) {
            rawContent = ":alias " + String.join(" ", aliasArguments) + " " + rawContent;
        }

        this.setContent(rawContent)
            .setAuthor(message.getAuthor())
            .setTime(message.getCreationTime())
            .setEditedTime(message.getEditedTime())
            .setAttachments(message.getAttachments())
            .setEmbeds(message.getEmbeds())
            .setMentionedChannels(message.getMentionedChannels())
            .setMentionedRoles(message.getMentionedRoles())
            .setMentionedUsers(prepareMentionedUsers(message.getMentionedUsers()))
            .setMentionsEveryone(message.mentionsEveryone())
            .setReactions(message.getReactions())
            .setPinned(message.isPinned())
            .setTTS(message.isTTS());
    }

    public CommandMessage(Message message, boolean mentionableCommand) {
        this(message, mentionableCommand, null);
    }

    public CommandMessage(String message) {
        super(0L, null, false, MessageType.UNKNOWN);

        this.mentionableCommand = false;
        this.setContent(message);
    }

    private String prepareRawContent(String content, boolean isAliasCommand) {
        String[] split = content.split(" ");

        if (!isMentionableCommand()) {
            if (!isAliasCommand) return content;
            return String.join(" ", Arrays.copyOfRange(split, 1, split.length));
        }
        return String.join(" ", Arrays.copyOfRange(split, isAliasCommand ? 2 : 1, split.length));
    }

    private List<User> prepareMentionedUsers(List<User> mentionedUsers) {
        if (!mentionableCommand) {
            return mentionedUsers;
        }
        List<User> users = new ArrayList<>();
        for (int i = 1; i < mentionedUsers.size(); i++) {
            users.add(mentionedUsers.get(i));
        }
        return Collections.unmodifiableList(users);
    }

    public boolean isMentionableCommand() {
        return mentionableCommand;
    }
}
