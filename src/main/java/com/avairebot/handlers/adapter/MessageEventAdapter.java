package com.avairebot.handlers.adapter;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.Statistics;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.contracts.commands.ThreadCommand;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.middleware.MiddlewareStack;
import com.avairebot.modules.SlowmodeModule;
import com.avairebot.utilities.ArrayUtil;
import com.avairebot.utilities.LevelUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MessageEventAdapter extends EventAdapter {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    private static final String mentionMessage = String.join("\n", Arrays.asList(
        "Hi there! I'm **%s**, a multipurpose Discord bot built for fun by %s!",
        "You can see what commands I have by using the `%s` command.",
        "",
        "I am currently running **AvaIre v%s**",
        "",
        "You can find all of my source code on github:",
        "https://github.com/avaire/avaire",
        "",
        "If you like me please vote for AvaIre to help me grow:",
        "https://discordbots.org/bot/avaire/vote"
    ));

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public MessageEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        Statistics.addMessage();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannelType().isGuild() && !event.getTextChannel().canTalk()) {
            return;
        }

        loadDatabasePropertiesIntoMemory(event).thenAccept(properties -> {
            if (!avaire.areWeReadyYet()) {
                return;
            }

            if (isUserBeingThrottledBySlowmodeInChannel(event, properties)) {
                event.getMessage().delete().queue();
                Metrics.slowmodeRatelimited.labels(event.getChannel().getId()).inc();
                return;
            }

            if (properties.getGuild() != null && properties.getPlayer() != null) {
                LevelUtil.rewardPlayer(avaire, event, properties.getGuild(), properties.getPlayer());
            }

            CommandContainer container = CommandHandler.getCommand(avaire, event.getMessage(), event.getMessage().getContentRaw());
            if (container != null && canExecuteCommand(event, container)) {
                Statistics.addCommands();

                invokeMiddlewareStack(new MiddlewareStack(avaire, event.getMessage(), container));
                return;
            }

            if (isMentionableAction(event)) {
                container = CommandHandler.getLazyCommand(ArrayUtil.toArguments(event.getMessage().getContentRaw())[1]);
                if (container != null && canExecuteCommand(event, container)) {
                    Statistics.addCommands();

                    invokeMiddlewareStack(new MiddlewareStack(avaire, event.getMessage(), container, true));
                    return;
                }

                if (avaire.getIntelligenceManager().isEnabled()) {
                    if (isAIEnabledForChannel(event, properties.getGuild())) {
                        avaire.getIntelligenceManager().request(
                            event.getMessage(), event.getMessage().getContentStripped()
                        );
                    }
                    return;
                }
            }

            if (isSingleBotMention(event.getMessage().getContentRaw().trim())) {
                sendTagInformationMessage(event);
                return;
            }

            if (!event.getChannelType().isGuild()) {
                sendInformationMessage(event);
            }
        });
    }

    private void invokeMiddlewareStack(MiddlewareStack stack) {
        if (stack.getCommand() instanceof ThreadCommand) {
            ((ThreadCommand) stack.getCommand()).submitTask(stack::next);
            return;
        }
        stack.next();
    }

    private boolean canExecuteCommand(MessageReceivedEvent event, CommandContainer container) {
        if (!container.getCommand().isAllowedInDM() && !event.getChannelType().isGuild()) {
            MessageFactory.makeWarning(event.getMessage(), ":warning: You can not use this command in direct messages!").queue();
            return false;
        }
        return true;
    }

    private boolean isMentionableAction(MessageReceivedEvent event) {
        if (!event.getMessage().isMentioned(avaire.getSelfUser())) {
            return false;
        }

        String[] args = event.getMessage().getContentRaw().split(" ");
        return args.length >= 2 &&
            userRegEX.matcher(args[0]).matches() &&
            event.getMessage().getMentionedUsers().get(0).getId().equals(avaire.getSelfUser().getId());

    }

    private boolean isSingleBotMention(String rawContent) {
        return rawContent.equals("<@" + avaire.getSelfUser().getId() + ">") ||
            rawContent.equals("<!@" + avaire.getSelfUser().getId() + ">");
    }

    private boolean isAIEnabledForChannel(MessageReceivedEvent event, GuildTransformer transformer) {
        if (transformer == null) {
            return true;
        }

        ChannelTransformer channel = transformer.getChannel(event.getChannel().getId());
        return channel == null || channel.getAI().isEnabled();
    }

    private boolean isUserBeingThrottledBySlowmodeInChannel(MessageReceivedEvent event, DatabaseProperties properties) {
        if (!event.getMessage().getChannelType().isGuild()) {
            return false;
        }

        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            return false;
        }

        ChannelTransformer channel = properties.getGuild().getChannel(event.getChannel().getId());
        if (channel == null || !channel.getSlowmode().isEnabled()) {
            return false;
        }

        String fingerprint = String.format("slowmode.%s.%s.%s",
            event.getGuild().getId(),
            event.getChannel().getId(),
            event.getAuthor().getId()
        );

        return SlowmodeModule.isThrottled(avaire, fingerprint, channel.getSlowmode().getLimit(), channel.getSlowmode().getDecay());
    }

    private void sendTagInformationMessage(MessageReceivedEvent event) {
        String author = "**Senither#0001**";
        if (event.getMessage().getChannelType().isGuild() && event.getGuild().getMemberById(88739639380172800L) != null) {
            author = "<@88739639380172800>";
        }

        MessageFactory.makeEmbeddedMessage(event.getMessage().getChannel(), Color.decode("#E91E63"), String.format(mentionMessage,
            avaire.getSelfUser().getName(),
            author,
            CommandHandler.getLazyCommand("help").getCommand().generateCommandTrigger(event.getMessage()),
            AppInfo.getAppInfo().VERSION
        ))
            .setFooter("This message will be automatically deleted in one minute.")
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));
    }

    private void sendInformationMessage(MessageReceivedEvent event) {
        try {
            ArrayList<String> strings = new ArrayList<>();
            strings.addAll(Arrays.asList(
                "To invite me to your server, use this link:",
                "*:oauth*",
                "",
                "You can use `.help` to see a list of all the categories of commands.",
                "You can use `.help category` to see a list of commands for that category.",
                "For specific command help, use `.help command` (for example `.help !ping`, `.help ping` also works)"
            ));

            if (avaire.getIntelligenceManager().isEnabled()) {
                strings.add("\nYou can tag me in a message with <@:botId> to send me a message that I should process using my AI.");
            }

            strings.add("\n**Full list of commands**\n*https://avairebot.com/docs/commands*");
            strings.add("\nAvaIre Support Server:\n*https://avairebot.com/support*");

            MessageFactory.makeEmbeddedMessage(event.getMessage(), Color.decode("#E91E63"), String.join("\n", strings))
                .set("oauth", avaire.getConfig().getString("discord.oauth"))
                .set("botId", avaire.getSelfUser().getId())
                .queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CompletableFuture<DatabaseProperties> loadDatabasePropertiesIntoMemory(final MessageReceivedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (!event.getChannelType().isGuild()) {
                return new DatabaseProperties(null, null);
            }

            GuildTransformer guild = GuildController.fetchGuild(avaire, event.getMessage());
            if (guild == null || !guild.isLevels()) {
                return new DatabaseProperties(guild, null);
            }
            return new DatabaseProperties(guild, PlayerController.fetchPlayer(avaire, event.getMessage()));
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
