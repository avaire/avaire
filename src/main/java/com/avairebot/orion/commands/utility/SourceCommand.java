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

    private final String rootUrl = "https://github.com/AvaIre/Orion";
    private final String commandUrl = "https://github.com/AvaIre/Orion/tree/master/src/main/java/com/avairebot/orion/commands/%s/%s.java";

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
            MessageFactory.makeInfo(message, "Orion source code:\n\n%s", rootUrl).queue();
            return true;
        }

        CommandContainer command = CommandHandler.getCommand(message);
        if (command == null) {
            MessageFactory.makeInfo(message, "Invalid command given, here is the full source code instead.\n\n%s", rootUrl).queue();
            return true;
        }

        String[] split = command.getCommand().getClass().toString().split("\\.");
        String category = split[split.length - 2];
        String name = split[split.length - 1];

        MessageFactory.makeInfo(message, "AvaIre source code for the **%s** command:\n\n" + commandUrl,
            command.getCommand().getName(), category, name
        ).queue();

        return true;
    }
}
