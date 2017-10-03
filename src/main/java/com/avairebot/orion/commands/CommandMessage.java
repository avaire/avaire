package com.avairebot.orion.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.MessageImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMessage extends MessageImpl {

    private final boolean mentionableCommand;

    public CommandMessage(Message message, boolean mentionableCommand) {
        super(message.getIdLong(), message.getChannel(), message.isWebhookMessage(), message.getType());

        this.mentionableCommand = mentionableCommand;
        this.setContent(prepareRawContent(message.getRawContent()))
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

    private String prepareRawContent(String content) {
        if (!mentionableCommand) {
            return content;
        }
        String[] split = content.split(" ");
        return String.join(" ", Arrays.copyOfRange(split, 1, split.length));
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
