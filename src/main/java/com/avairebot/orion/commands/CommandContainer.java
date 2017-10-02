package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.Command;

public class CommandContainer {

    public final Command command;
    public final Category category;

    public CommandContainer(Command command, Category category) {
        this.command = command;
        this.category = category;
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
}
