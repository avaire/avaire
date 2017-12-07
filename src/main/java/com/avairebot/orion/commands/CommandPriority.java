package com.avairebot.orion.commands;

public enum CommandPriority {

    HIDDEN(3),
    IGNORED(0),
    LOW(1),
    LOWEST(2),
    NORMAL(3),
    HIGH(4),
    HIGHEST(5);

    private final int priority;

    CommandPriority(int priority) {
        this.priority = priority;
    }

    public boolean isGreaterThan(CommandPriority commandPriority) {
        return priority > commandPriority.priority;
    }
}
