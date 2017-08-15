package com.avairebot.orion;

public class Statistics {

    private static int messages = 0;
    private static int commands = 0;
    private static int queries = 0;

    public static void addMessage() {
        messages++;
    }

    public static void addCommands() {
        commands++;
    }

    public static void addQueries() {
        queries++;
    }

    public static int getMessages() {
        return messages;
    }

    public static int getCommands() {
        return commands;
    }

    public static int getQueries() {
        return queries;
    }
}
