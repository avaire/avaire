package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.CompletableFuture;

public class MessageCreateEvent extends EventHandler {

    public MessageCreateEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Statistics.addMessage();

        if (event.getAuthor().isBot()) {
            return;
        }

        loadDatabasePropertiesIntoMemory(event).thenAccept(guild -> {
            CommandContainer container = CommandHandler.getCommand(event.getMessage());
            if (container != null) {
                Statistics.addCommands();

                if (!container.getCommand().isAllowedInDM() && !event.getChannelType().isGuild()) {
                    MessageFactory.makeWarning(event.getMessage(), ":warning: You can not use this command in direct messages!").queue();
                    return;
                }

                (new MiddlewareStack(orion, event.getMessage(), container)).next();
            }
        });
    }

    private CompletableFuture<GuildTransformer> loadDatabasePropertiesIntoMemory(final MessageReceivedEvent event) {
        return CompletableFuture.supplyAsync(() -> GuildController.fetchGuild(orion, event.getMessage()));
    }
}
