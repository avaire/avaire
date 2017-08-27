package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.AbstractCommand;

public class CommandContainer {

    public final AbstractCommand command;
    public final Category category;

    public CommandContainer(AbstractCommand command, Category category) {
        this.command = command;
        this.category = category;
    }

    public AbstractCommand getCommand() {
        return command;
    }

    public Category getCategory() {
        return category;
    }

    public String getDefaultPrefix() {
        return category.getPrefix();
    }
}
