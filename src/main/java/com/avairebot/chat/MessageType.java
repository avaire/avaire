package com.avairebot.chat;

import java.awt.*;

public enum MessageType {

    ERROR("#EF5350"),
    WARNING("#FAA61A"),
    SUCCESS("#43B581"),
    INFO("#3A71C1");

    private final String color;

    MessageType(String color) {
        this.color = color;
    }

    public Color getColor() {
        return Color.decode(this.color);
    }
}
