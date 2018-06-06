package com.avairebot.modlog;

import com.avairebot.chat.MessageType;

import java.awt.*;

public enum ModlogType {

    KICK(1, "Kick", MessageType.WARNING.getColor()),
    VOICE_KICK(2, "Voice Kick", MessageType.WARNING.getColor()),
    SOFT_BAN(3, "Soft Ban", MessageType.ERROR.getColor()),
    BAN(4, "Ban", MessageType.ERROR.getColor()),
    PURGE(5, "Purge", MessageType.INFO.getColor()),
    WARN(6, "Warning", MessageType.WARNING.getColor());

    final int id;
    final String name;
    final Color color;

    ModlogType(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public static ModlogType fromId(int id) {
        for (ModlogType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
