package com.avairebot.orion.handlers;

public enum EventTypes {
    MessageReceivedEvent(MessageCreateEvent.class);

    private final Class instance;

    EventTypes(Class instance) {
        this.instance = instance;
    }

    public <T> Class<T> getInstance() {
        return instance;
    }
}
