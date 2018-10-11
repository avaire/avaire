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

import com.avairebot.audio.AudioTrackContainer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.Event;

@SuppressWarnings("unused")
public class NowPlayingEvent extends Event {

    private final long guildId;
    private final Guild guild;
    private final AudioTrackContainer container;

    /**
     * Creates the new music now playing event.
     *
     * @param api       The JDA api(shard) that the event should be triggered on.
     * @param guild     The guild that the audio track is now playing in.
     * @param container The audio track container for the track now playing in the guild.
     */
    public NowPlayingEvent(JDA api, Guild guild, AudioTrackContainer container) {
        super(api);

        this.guildId = guild.getIdLong();
        this.guild = guild;
        this.container = container;
    }

    /**
     * Gets the ID of the guild that the {@link #getContainer() audio track} is now playing in.
     *
     * @return The ID of the guild the audio track is now playing in.
     */
    public long getGuildId() {
        return guildId;
    }


    /**
     * Gets the JDA guild object that the {@link #getContainer() audio track} is now playing in.
     *
     * @return The JDA guild object that the audio track is now playing in.
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Gets the audio track container, the container will hold information
     * about the audio track, and its current state for the guild.
     *
     * @return The audio track container that the event was fired for.
     */
    public AudioTrackContainer getContainer() {
        return container;
    }

    /**
     * Gets the title of the song from the audio track meta data, if
     * the title is null, the track URL will be used instead.
     *
     * @return The title of the song from the audio track.
     */
    public String getSongTitle() {
        String songTitle = container.getAudioTrack().getInfo().title;
        if (songTitle == null || songTitle.equalsIgnoreCase("Unknown Title")) {
            return container.getAudioTrack().getInfo().uri;
        }
        return songTitle;
    }
}
