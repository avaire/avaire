package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.Command;

public class CommandContainer {

    private final Command command;
    private final Category category;
    private final CommandPriority priority;

    private final String sourceUri;

    public CommandContainer(Command command, Category category, String sourceUri) {
        this.command = command;
        this.category = category;
        this.priority = command.getCommandPriority();
        this.sourceUri = sourceUri;
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

    public String getSourceUri() {
        return sourceUri;
    }
}
