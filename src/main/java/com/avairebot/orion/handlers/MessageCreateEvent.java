package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MessageCreateEvent extends EventHandler {

    private final Pattern argumentsRegEX;

    public MessageCreateEvent(Orion orion) {
        super(orion);

        this.argumentsRegEX = Pattern.compile("[\\s\"]+|\"([^\"]*)\"", Pattern.MULTILINE);
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

            String[] arguments = this.argumentsRegEX.split(e.getMessage().getContent());

            User author = e.getMessage().getAuthor();
            this.orion.logger.info("Executing Command <" + e.getMessage().getContent() + "> from " + author.getName() + "#" + author.getDiscriminator());
            container.getCommand().onCommand(e, Arrays.copyOfRange(arguments, 1, arguments.length));
        }
    }
}
