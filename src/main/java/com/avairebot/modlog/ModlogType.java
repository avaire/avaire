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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public enum ModlogType {

    /**
     * Represents the action when a user is kicked from a server.
     */
    KICK(1, "Kick", "\uD83D\uDC62", true, MessageType.WARNING.getColor()),

    /**
     * Represents the action when a user is kicked from a voice channel.
     */
    VOICE_KICK(2, "Voice Kick", "\uD83D\uDC62", false, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is banned from a server without the
     * message they sent over the last 7 days are left alone.
     */
    SOFT_BAN(3, "Soft Ban", "\uD83D\uDD28", true, MessageType.ERROR.getColor()),

    /**
     * Represents when a user is banned from a server, including
     * deleting any message they have sent in the last 7 days.
     */
    BAN(4, "Ban", "\uD83D\uDD28", true, MessageType.ERROR.getColor()),

    /**
     * Represents when multiple messages are deleted from a channel.
     */
    PURGE(5, "Purge", null, false, MessageType.INFO.getColor()),

    /**
     * Represents when a user is warned through Avas warn system.
     */
    WARN(6, "Warn", "\uD83D\uDCE2", true, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is unbanned from a server through Ava.
     */
    UNBAN(7, "Unban", null, false, false, MessageType.SUCCESS.getColor()),

    /**
     * Represents when a user is pardoned for an old modlog case.
     */
    PARDON(8, "Pardon", null, false, false, MessageType.SUCCESS.getColor());

    final int id;
    final String name;
    @Nullable
    final String emote;
    final boolean notifyable;
    final boolean punishment;
    final Color color;

    ModlogType(int id, String name, String emote, boolean notifyable, Color color) {
        this(id, name, emote, notifyable, true, color);
    }

    ModlogType(int id, String name, String emote, boolean notifyable, boolean punishment, Color color) {
        this.id = id;
        this.name = name;
        this.emote = emote;
        this.notifyable = notifyable;
        this.punishment = punishment;
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
     * Gets the emote that is associated with the modlog type.
     *
     * @return The emote that is associated with the modlog type.
     */
    @Nonnull
    public String getEmote() {
        return emote == null ? "" : emote;
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
        if (getName().equalsIgnoreCase("ban") || getName().equalsIgnoreCase("soft ban")) {
            return getName().toLowerCase() + "ned";
        }
        return getName().toLowerCase() + "ed";
    }

    /**
     * Checks if the current modlog type is a punishment or not.
     *
     * @return <code>True</code> if the modlog type is a punishment, <code>False</code> otherwise.
     */
    public boolean isPunishment() {
        return punishment;
    }
}
