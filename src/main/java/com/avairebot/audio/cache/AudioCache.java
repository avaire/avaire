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

@SuppressWarnings("unused")
public class AudioCache extends Evalable {

    private String trackUrl;
    private long requestedBy;
    private long position;

    /**
     * Create the audio cache instance using the given track
     * url, user request ID, and the track position.
     *
     * @param trackUrl    The url to the track.
     * @param requestedBy The ID of the user that requested the track.
     * @param position    The track position in milliseconds.
     */
    AudioCache(String trackUrl, long requestedBy, long position) {
        this.trackUrl = trackUrl;
        this.requestedBy = requestedBy;
        this.position = position;
    }

    /**
     * Create the audio cache instance using the given
     * track url, and the user request ID.
     *
     * @param trackUrl    The url to the track.
     * @param requestedBy The ID of the user that requested the track.
     */
    AudioCache(String trackUrl, long requestedBy) {
        this(trackUrl, requestedBy, -1);
    }

    /**
     * Create the audio cache instance using the given audio track container.
     *
     * @param container The audio track container that should be
     *                  used to create the audio cache instance.
     */
    AudioCache(AudioTrackContainer container) {
        this(
            container.getAudioTrack().getInfo().uri,
            container.getRequester().getIdLong(),
            container.getAudioTrack().getPosition()
        );
    }

    /**
     * The URL of the track.
     *
     * @return The URL of the track.
     */
    public String getTrackUrl() {
        return trackUrl;
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
