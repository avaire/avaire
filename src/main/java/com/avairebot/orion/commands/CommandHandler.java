package com.avairebot.orion.commands;

import net.dv8tion.jda.core.entities.Message;

import java.util.*;

public class CommandHandler {

    private static final Map<List<String>, Command> commands = new HashMap<>();

    public static Command getCommand(Message message) {
        String trigger = message.getContent().split(" ")[0].toLowerCase();

        for (Map.Entry<List<String>, Command> entry : commands.entrySet()) {
            if (entry.getKey().contains(trigger)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static boolean register(Command command) {
        for (String trigger : command.getTriggers()) {
            for (Map.Entry<List<String>, Command> entry : commands.entrySet()) {
                if (entry.getKey().contains(trigger.toLowerCase())) {
                    return false;
                }
            }
        }

        commands.put(command.getTriggers(), command);
        return true;
    }

    public static Collection<Command> getCommands() {
        return commands.values();
    }
}
