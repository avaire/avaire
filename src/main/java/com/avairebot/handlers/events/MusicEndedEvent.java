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

package com.avairebot.handlers.events;

import com.avairebot.contracts.events.Cancellable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.Event;

public class MusicEndedEvent extends Event implements Cancellable {

    private final Guild guild;
    private final long guildId;
    private boolean cancelled;

    /**
     * Creates a new music has ended even.
     *
     * @param api   The JDA api(shard) that the event should be triggered on.
     * @param guild The guild that the music has ended in.
     */
    public MusicEndedEvent(JDA api, Guild guild) {
        super(api);

        this.guild = guild;
        this.guildId = guild.getIdLong();
    }

    /**
     * The JDA guild instance that the music has ended in.
     *
     * @return The JDA guild instance that the music has ended in.
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * The ID of the guild that the music has ended in.
     *
     * @return The ID of the guild that the music has ended in.
     */
    public long getGuildId() {
        return guildId;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
