package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MessageCreateEvent extends EventHandler {

    public MessageCreateEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Statistics.addMessage();

        if (e.getAuthor().isBot()) {
            return;
        }

        CommandContainer container = CommandHandler.getCommand(e.getMessage());
        if (container != null) {
            Statistics.addCommands();

            (new MiddlewareStack(orion, e, container)).next();
        }
    }
}
