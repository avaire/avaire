package com.avairebot.orion.commands;

public class Category {

    private final String name;
    private final String prefix;

    public Category(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
