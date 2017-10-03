package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.exceptions.InvalidCommandPrefixException;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.Checks;

import java.util.*;

public class CommandHandler {

    private static final Map<List<String>, CommandContainer> COMMANDS = new HashMap<>();

    public static CommandContainer getCommand(Command command) {
        for (Map.Entry<List<String>, CommandContainer> entry : COMMANDS.entrySet()) {
            if (entry.getValue().getCommand().isSame(command)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static CommandContainer getCommand(Message message) {
        return getCommand(message.getContent().split(" ")[0].toLowerCase());
    }

    public static CommandContainer getCommand(String commandTrigger) {
        for (Map.Entry<List<String>, CommandContainer> entry : COMMANDS.entrySet()) {
            for (String trigger : entry.getKey()) {
                if (commandTrigger.equalsIgnoreCase(entry.getValue().getDefaultPrefix() + trigger)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public static CommandContainer getCommandWithPriority(String commandTrigger) {
        List<CommandContainer> commands = new ArrayList<>();
        for (Map.Entry<List<String>, CommandContainer> entry : COMMANDS.entrySet()) {
            if (entry.getValue().getPriority().equals(CommandPriority.IGNORED)) {
                continue;
            }

            COMMAND_TRIGGER_LOOP:
            for (String trigger : entry.getKey()) {
                if (commandTrigger.equalsIgnoreCase(trigger)) {
                    commands.add(entry.getValue());
                }
            }
        }

        if (commands.isEmpty()) {
            return null;
        }

        if (commands.size() == 1) {
            return commands.get(0);
        }

        return commands.stream().sorted((first, second) -> {
            if (first.getPriority().equals(second.getPriority())) {
                return 0;
            }
            return first.getPriority().isGreaterThan(second.getPriority()) ? -1 : 1;
        }).findFirst().get();
    }

    public static void register(Command command) {
        Category category = Category.fromCommand(command);
        Checks.notNull(category, String.format("%s :: %s", command.getName(), "Invalid command category, command category"));
        Checks.notNull(command.getDescription(), String.format("%s :: %s", command.getName(), "Command description"));

        for (String trigger : command.getTriggers()) {
            for (Map.Entry<List<String>, CommandContainer> entry : COMMANDS.entrySet()) {
                for (String subTrigger : entry.getKey()) {
                    if (Objects.equals(category.getPrefix() + trigger, entry.getValue().getDefaultPrefix() + subTrigger)) {
                        throw new InvalidCommandPrefixException(category.getPrefix() + trigger, command.getName(), entry.getValue().getCommand().getName());
                    }
                }
            }
        }

        COMMANDS.put(command.getTriggers(), new CommandContainer(command, category));
    }

    public static Collection<CommandContainer> getCommands() {
        return COMMANDS.values();
    }
}
