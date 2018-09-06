package com.avairebot.vote;

public enum VoteMetricType {

    WEBHOOK("Webhook"),
    COMMAND("Command");

    private final String name;

    VoteMetricType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
