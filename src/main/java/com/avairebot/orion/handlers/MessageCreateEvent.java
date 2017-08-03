package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MessageCreateEvent extends EventHandler {

    public MessageCreateEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }


        String content = e.getMessage().getContent();
        this.orion.logger.info(e.getAuthor().getName() + " said: " + content);

        if (content.equalsIgnoreCase("!ping")) {
            e.getMessage().getChannel().sendMessage("Pong! Time taken " + e.getJDA().getPing() + " ms!").queue();
        }
    }
}
