/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.modlog;

import com.avairebot.chat.MessageType;

import javax.annotation.Nullable;
import java.awt.*;

public enum ModlogType {

    /**
     * Represents the action when a user is kicked from a server.
     */
    KICK(1, "Kick", true, MessageType.WARNING.getColor()),

    /**
     * Represents the action when a user is kicked from a voice channel.
     */
    VOICE_KICK(2, "Voice Kick", false, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is banned from a server without the
     * message they sent over the last 7 days are left alone.
     */
    SOFT_BAN(3, "Soft Ban", true, MessageType.ERROR.getColor()),

    /**
     * Represents when a user is banned from a server, including
     * deleting any message they have sent in the last 7 days.
     */
    BAN(4, "Ban", true, MessageType.ERROR.getColor()),

    /**
     * Represents when multiple messages are deleted from a channel.
     */
    PURGE(5, "Purge", false, MessageType.INFO.getColor()),

    /**
     * Represents when a user is warned through Avas warn system.
     */
    WARN(6, "Warn", true, MessageType.WARNING.getColor());

    final int id;
    final String name;
    final boolean notifyable;
    final Color color;

    ModlogType(int id, String name, boolean notifyable, Color color) {
        this.id = id;
        this.name = name;
        this.notifyable = notifyable;
        this.color = color;
    }

    /**
     * Tries to get a modlog type by ID, if no modlog types exists with
     * the given ID, <code>null</code> will be returned instead.
     *
     * @param id The ID that the modlog type should have.
     * @return Possibly-null, the modlog tpe with the given ID.
     */
    public static ModlogType fromId(int id) {
        for (ModlogType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the ID of the current modlog type.
     *
     * @return The ID of the current modlog type.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the current modlog type.
     *
     * @return The name of the current modlog type.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color of the current modlog type.
     *
     * @return The color of the current modlog type.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the notify name for the current modlog type.
     *
     * @return The notify name for the current modlog type.
     */
    @Nullable
    public String getNotifyName() {
        if (!notifyable) {
            return null;
        }
        return getName().toLowerCase() + "ed";
    }
}
