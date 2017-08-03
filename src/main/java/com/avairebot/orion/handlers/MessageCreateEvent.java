package com.avairebot.orion.handlers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageCreateEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        String content = e.getMessage().getContent();
        if (content.equalsIgnoreCase("!ping")) {
            e.getMessage().getChannel().sendMessage("Pong! Time taken " + e.getJDA().getPing() + " ms!").queue();
        }
    }
}
