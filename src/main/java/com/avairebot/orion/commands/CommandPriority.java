package com.avairebot.orion.commands;

public enum CommandPriority {

    IGNORED(-1),
    LOW(0),
    LOWEST(1),
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
