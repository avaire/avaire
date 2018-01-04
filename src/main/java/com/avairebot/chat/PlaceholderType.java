package com.avairebot.chat;

import com.avairebot.contracts.chat.PlaceholderFunction;

public enum PlaceholderType {

    ALL,
    GUILD(DefaultPlaceholders::toGuild),
    CHANNEL(DefaultPlaceholders::toChannel),
    USER(DefaultPlaceholders::toUser);

    private final PlaceholderFunction function;

    PlaceholderType(PlaceholderFunction function) {
        this.function = function;
    }

    PlaceholderType() {
        this.function = null;
    }

    public PlaceholderFunction getFunction() {
        return function;
    }

    public String parse(Object object, String message) {
        return DefaultPlaceholders.parse(this, object, message);
    }
}
