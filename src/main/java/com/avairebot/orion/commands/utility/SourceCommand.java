package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SourceCommand extends Command {

    private final String rootUrl = "https://github.com/avaire/orion";

    public SourceCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Source Command";
    }

    @Override
    public String getDescription() {
        return "Gives you the source code for the Bot, or the code for a given command.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Returns the full source code for the bot.",
            "`:command <command>` - Returns the source code for the given command."
        );
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("source");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeInfo(message, "Orion source code:\n\n" + rootUrl).queue();
            return true;
        }

        CommandContainer command = getCommand(message, args[0]);
        if (command == null) {
            MessageFactory.makeInfo(message, "Invalid command given, here is the full source code instead.\n\n" + rootUrl).queue();
            return true;
        }

        String sourceUri = command.getSourceUri();
        if (sourceUri == null) {
            MessageFactory.makeInfo(message,
                "The command is registered via an external plugin, the author of the plugin " +
                    "haven't made the source of the command public, or they forgot to add a link to " +
                    "it, here is the full source code for the bot instead.\n\n" + rootUrl
            ).queue();
            return true;
        }

        MessageFactory.makeInfo(message, "AvaIre source code for the **:command** command:\n\n:url")
            .set("command", command.getCommand().getName())
            .set("url", sourceUri)
            .queue();

        return true;
    }

    private CommandContainer getCommand(Message message, String commandString) {
        CommandContainer command = CommandHandler.getCommand(message, commandString);
        if (command != null) {
            return command;
        }
        return CommandHandler.getLazyCommand(commandString);
    }
}
