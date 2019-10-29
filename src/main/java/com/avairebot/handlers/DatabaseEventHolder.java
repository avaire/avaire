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

package com.avairebot.handlers;

import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;

import javax.annotation.Nullable;

public class DatabaseEventHolder {

    private final GuildTransformer guild;
    private final PlayerTransformer player;

    /**
     * Creates a new database holder instance using
     * the given guild and player transformer.
     *
     * @param guild  The guild transformer that should be stored in the event holder.
     * @param player The player transformer that should be stored in the event holder.
     */
    public DatabaseEventHolder(GuildTransformer guild, PlayerTransformer player) {
        this.guild = guild;
        this.player = player;
    }

    /**
     * Gets the guild database transformer for the current JDA event,
     * the guild transformer can be used to pull specific bot server
     * settings and information about the current guild.
     *
     * @return The guild database transformer for the current JDA event,
     *         or {@code NULL} if the event was not invoked for a guild.
     */
    public GuildTransformer getGuild() {
        return guild;
    }

    /**
     * Gets the player database transformer for the current JDA event,
     * the player transformer can be used to pull player experience
     * information about the user who invoked the JDA event.
     *
     * @return The player database transformer for the user who invoked the JDA event,
     *         or {@code NULL} if the leveling feature is disabled in the guild.
     */
    @Nullable
    public PlayerTransformer getPlayer() {
        return player;
    }
}
