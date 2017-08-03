package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Command;
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
        if (e.getAuthor().isBot()) {
            return;
        }

        Command command = CommandHandler.getCommand(e.getMessage());
        if (command != null) {
            String[] arguments = this.argumentsRegEX.split(e.getMessage().getContent());

            User author = e.getMessage().getAuthor();
            this.orion.logger.info("Executing Command <" + e.getMessage().getContent() + "> from " + author.getName() + "#" + author.getDiscriminator());
            command.onCommand(e, Arrays.copyOfRange(arguments, 1, arguments.length));
        }
    }
}
