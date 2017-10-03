package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.Command;

public class CommandContainer {

    public final Command command;
    public final Category category;
    public final CommandPriority priority;

    public CommandContainer(Command command, Category category) {
        this.command = command;
        this.category = category;
        this.priority = command.getCommandPriority();
    }

    public Command getCommand() {
        return command;
    }

    public Category getCategory() {
        return category;
    }

    public String getDefaultPrefix() {
        return category.getPrefix();
    }

    public CommandPriority getPriority() {
        return priority;
    }
}
