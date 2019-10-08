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

package com.avairebot.audio.cache;

import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.contracts.debug.Evalable;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AudioCache extends Evalable implements Serializable {

    private byte[] track;
    private long requestedBy;
    private long position;

    /**
     * Create the audio cache instance using the given track
     * url, user request ID, and the track position.
     *
     * @param audioTrack  The LavaPlayer AudioTrack object instance.
     * @param requestedBy The ID of the user that requested the track.
     * @param position    The track position in milliseconds.
     */
    AudioCache(AudioTrack audioTrack, long requestedBy, long position) {
        this.requestedBy = requestedBy;
        this.position = position;
        this.track = AudioTrackSerializer.encodeTrack(audioTrack);
    }

    /**
     * Create the audio cache instance using the given
     * track url, and the user request ID.
     *
     * @param audioTrack  The LavaPlayer AudioTrack object instance.
     * @param requestedBy The ID of the user that requested the track.
     */
    AudioCache(AudioTrack audioTrack, long requestedBy) {
        this(audioTrack, requestedBy, -1);
    }

    /**
     * Create the audio cache instance using the given audio track container.
     *
     * @param container The audio track container that should be
     *                  used to create the audio cache instance.
     */
    AudioCache(AudioTrackContainer container) {
        this(
            container.getAudioTrack(),
            container.getRequester().getIdLong(),
            container.getAudioTrack().getPosition()
        );
    }

    /**
     * The encoded version of the audio track.
     *
     * @return The URL of the track.
     */
    public byte[] getTrack() {
        return track;
    }

    /**
     * The ID of the user that requested the track.
     *
     * @return The ID of the user that requested the track.
     */
    public long getRequestedBy() {
        return requestedBy;
    }

    /**
     * The track position in milliseconds.
     *
     * @return The track position in milliseconds.
     */
    public long getPosition() {
        return position;
    }
}
