package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.controllers.PlayerController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.level.LevelManager;
import com.avairebot.orion.middleware.MiddlewareStack;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MessageCreateEvent extends EventHandler {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    public MessageCreateEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Statistics.addMessage();

        if (event.getAuthor().isBot()) {
            return;
        }

        loadDatabasePropertiesIntoMemory(event).thenAccept(properties -> {
            if (properties.getGuild() != null && properties.getPlayer() != null) {
                LevelManager.rewardPlayer(orion, event, properties.getGuild(), properties.getPlayer());
            }

            CommandContainer container = CommandHandler.getCommand(event.getMessage());
            if (container != null && canExecuteCommand(event, container)) {
                Statistics.addCommands();

                (new MiddlewareStack(orion, event.getMessage(), container)).next();
            }

            if (isMentionableCommand(event)) {
                container = CommandHandler.getCommandWithPriority(event.getMessage().getContent().split(" ")[1]);
                if (container != null && canExecuteCommand(event, container)) {
                    Statistics.addCommands();

                    (new MiddlewareStack(orion, event.getMessage(), container, true)).next();
                }
            }
        });
    }

    private boolean canExecuteCommand(MessageReceivedEvent event, CommandContainer container) {
        if (!container.getCommand().isAllowedInDM() && !event.getChannelType().isGuild()) {
            MessageFactory.makeWarning(event.getMessage(), ":warning: You can not use this command in direct messages!").queue();
            return false;
        }
        return true;
    }

    private boolean isMentionableCommand(MessageReceivedEvent event) {
        if (!event.getMessage().isMentioned(orion.getJDA().getSelfUser())) {
            return false;
        }

        String[] args = event.getMessage().getRawContent().split(" ");
        return args.length >= 2 &&
                userRegEX.matcher(args[0]).matches() &&
                event.getMessage().getMentionedUsers().get(0).getId().equals(orion.getJDA().getSelfUser().getId());

    }

    private CompletableFuture<DatabaseProperties> loadDatabasePropertiesIntoMemory(final MessageReceivedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (!event.getChannelType().isGuild()) {
                return new DatabaseProperties(null, null);
            }

            GuildTransformer guild = GuildController.fetchGuild(orion, event.getMessage());
            if (guild == null || !guild.isLevels()) {
                return new DatabaseProperties(guild, null);
            }
            return new DatabaseProperties(guild, PlayerController.fetchPlayer(orion, event.getMessage()));
        });
    }

    private class DatabaseProperties {

        private final GuildTransformer guild;
        private final PlayerTransformer player;

        DatabaseProperties(GuildTransformer guild, PlayerTransformer player) {
            this.guild = guild;
            this.player = player;
        }

        public GuildTransformer getGuild() {
            return guild;
        }

        public PlayerTransformer getPlayer() {
            return player;
        }
    }
}
