package com.avairebot.orion.logger;

public enum Level {

    INFO, WARNING, ERROR, EXCEPTION;

    public String getPrefix() {
        return String.format("[%s]", this.toString());
    }
}
