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
import com.avairebot.language.I18n;
import com.google.common.base.CaseFormat;
import net.dv8tion.jda.core.entities.Guild;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public enum ModlogType {

    /**
     * Represents the action when a user is kicked from a server.
     */
    KICK(1, "\uD83D\uDC62", true, MessageType.WARNING.getColor()),

    /**
     * Represents the action when a user is kicked from a voice channel.
     */
    VOICE_KICK(2, "\uD83D\uDC62", false, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is banned from a server without the
     * message they sent over the last 7 days are left alone.
     */
    SOFT_BAN(3, "\uD83D\uDD28", true, MessageType.ERROR.getColor()),

    /**
     * Represents when a user is banned from a server, including
     * deleting any message they have sent in the last 7 days.
     */
    BAN(4, "\uD83D\uDD28", true, MessageType.ERROR.getColor()),

    /**
     * Represents when multiple messages are deleted from a channel.
     */
    PURGE(5, null, false, MessageType.INFO.getColor()),

    /**
     * Represents when a user is warned through Avas warn system.
     */
    WARN(6, "\uD83D\uDCE2", true, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is unbanned from a server through Ava.
     */
    UNBAN(7, null, false, false, MessageType.SUCCESS.getColor()),

    /**
     * Represents when a user is pardoned for an old modlog case.
     */
    PARDON(8, null, false, false, MessageType.SUCCESS.getColor()),

    /**
     * Represents when a user is muted from a server.
     */
    MUTE(9, "\uD83D\uDD07", true, true, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is muted temporarily from a server.
     */
    TEMP_MUTE(10, "\uD83D\uDD07", true, true, MessageType.WARNING.getColor()),

    /**
     * Represents when a user is unmuted in a server.
     */
    UNMUTE(11, "\uD83D\uDD0A", true, false, MessageType.SUCCESS.getColor());

    final int id;
    @Nullable
    final String emote;
    final boolean notifyable;
    final boolean punishment;
    final Color color;

    ModlogType(int id, String emote, boolean notifyable, Color color) {
        this(id, emote, notifyable, true, color);
    }

    ModlogType(int id, @Nullable String emote, boolean notifyable, boolean punishment, Color color) {
        this.id = id;
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
     * @param guild The guild that requested the modlog type name.
     * @return The name of the current modlog type.
     */
    @Nonnull
    public String getName(Guild guild) {
        String name = loadNameProperty(guild, "name");
        if (name != null) {
            return name;
        }

        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name())
            .replaceAll("(.)([A-Z])", "$1 $2");
    }

    /**
     * Gets the notify name for the current modlog type.
     *
     * @param guild The guild that requested the modlog type name.
     * @return The notify name for the current modlog type.
     */
    @Nullable
    public String getNotifyName(Guild guild) {
        if (!notifyable) {
            return null;
        }
        return loadNameProperty(guild, "action");
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
     * Checks if the current modlog type is a punishment or not.
     *
     * @return <code>True</code> if the modlog type is a punishment, <code>False</code> otherwise.
     */
    public boolean isPunishment() {
        return punishment;
    }

    private String loadNameProperty(Guild guild, String type) {
        return I18n.getString(guild, String.format("modlog-types.%s.%s",
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name()),
            type
        ));
    }
}
