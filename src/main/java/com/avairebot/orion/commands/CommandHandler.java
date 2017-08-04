package com.avairebot.orion.commands;

import net.dv8tion.jda.core.entities.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    private static final Map<List<String>, CommandContainer> commands = new HashMap<>();

    public static CommandContainer getCommand(Message message) {
        String commandTrigger = message.getContent().split(" ")[0].toLowerCase();

        for (Map.Entry<List<String>, CommandContainer> entry : commands.entrySet()) {
            for (String trigger : entry.getKey()) {
                if (commandTrigger.equalsIgnoreCase(entry.getValue().getDefaultPrefix() + trigger)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public static boolean register(Command command) {
        for (String trigger : command.getTriggers()) {
            for (Map.Entry<List<String>, CommandContainer> entry : commands.entrySet()) {
                if (entry.getKey().contains(trigger.toLowerCase())) {
                    return false;
                }
            }
        }

        Category category = Category.fromCommand(command);
        if (category == null) {
            return false;
        }

        commands.put(command.getTriggers(), new CommandContainer(command, category));
        return true;
    }

    public static Collection<CommandContainer> getCommands() {
        return commands.values();
    }
}
