package com.avairebot.orion.commands;

import com.avairebot.orion.contracts.commands.AbstractCommand;

public enum Category {
    SYSTEM("System", ";"),
    UTILITY("Utility", "!");

    private final String name;
    private final String prefix;

    Category(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public static Category fromCommand(AbstractCommand command) {
        String commandPackage = command.getClass().getName().split("\\.")[4];

        for (Category category : Category.values()) {
            if (category.toString().equalsIgnoreCase(commandPackage)) {
                return category;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
