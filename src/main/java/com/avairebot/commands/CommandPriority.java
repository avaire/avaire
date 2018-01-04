package com.avairebot.commands;

public enum CommandPriority {

    HIDDEN(2),
    SYSTEM(1),
    IGNORED(0),
    LOWEST(1),
    LOW(2),
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
