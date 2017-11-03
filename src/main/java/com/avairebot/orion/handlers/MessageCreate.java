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
import com.avairebot.orion.middleware.MiddlewareStack;
import com.avairebot.orion.utilities.ArrayUtil;
import com.avairebot.orion.utilities.LevelUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MessageCreate extends EventHandler {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    private static final String mentionMessage = String.join("\n", Arrays.asList(
        "Hi there! I'm **%s**, a multipurpose Discord bot built for fun by %s!",
        "You can see what commands I have by using the `%s` command.",
        "",
        "I am currently running **Orion v%s**",
        "",
        "You can find all of my source code on github:",
        "https://github.com/avaire/orion"
    ));

    public MessageCreate(Orion orion) {
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
                LevelUtil.rewardPlayer(orion, event, properties.getGuild(), properties.getPlayer());
            }

            CommandContainer container = CommandHandler.getCommand(orion, event.getMessage(), event.getMessage().getRawContent());
            if (container != null && canExecuteCommand(event, container)) {
                Statistics.addCommands();

                (new MiddlewareStack(orion, event.getMessage(), container)).next();
            }

            if (isMentionableCommand(event)) {
                container = CommandHandler.getLazyCommand(ArrayUtil.toArguments(event.getMessage().getContent())[1]);
                if (container != null && canExecuteCommand(event, container)) {
                    Statistics.addCommands();

                    (new MiddlewareStack(orion, event.getMessage(), container, true)).next();
                }
            }

            if (isSingleBotMention(event.getMessage().getRawContent().trim())) {
                sendTagInformationMessage(event);
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

    private boolean isSingleBotMention(String rawContent) {
        return rawContent.equals("<@" + orion.getJDA().getSelfUser().getId() + ">") ||
            rawContent.equals("<!@" + orion.getJDA().getSelfUser().getId() + ">");
    }

    private void sendTagInformationMessage(MessageReceivedEvent event) {
        String author = "**Senither#8023**";
        if (event.getMessage().getChannelType().isGuild() && event.getGuild().getMemberById(88739639380172800L) != null) {
            author = "<@88739639380172800>";
        }

        event.getMessage().getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#E91E63"))
            .setDescription(String.format(mentionMessage,
                orion.getJDA().getSelfUser().getName(),
                author,
                CommandHandler.getLazyCommand("help").getCommand().generateCommandTrigger(event.getMessage()),
                orion.getVersion()
            ))
            .setFooter("This message will be automatically deleted in one minute.", null)
            .build()
        ).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));
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
