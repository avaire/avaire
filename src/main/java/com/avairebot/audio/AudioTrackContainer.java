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

package com.avairebot.audio;

import com.avairebot.contracts.debug.Evalable;
import com.avairebot.contracts.metadata.HasMetadata;
import com.avairebot.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioTrackContainer extends Evalable implements HasMetadata<String, Object> {

    private final AudioTrack audioTrack;
    private final User requester;
    private final List<Long> skips;
    private int playedTime;

    @Nullable
    private HashMap<String, Object> metadata;

    public AudioTrackContainer(AudioTrack audioTrack, User requester) {
        this.audioTrack = audioTrack;
        this.requester = requester;

        skips = new ArrayList<>();
        playedTime = 0;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public User getRequester() {
        return requester;
    }

    public List<Long> getSkips() {
        return skips;
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public void incrementPlayedTime() {
        playedTime++;
    }

    public String getFormattedPlayedTime() {
        return NumberUtil.formatTime(getAudioTrack().getPosition());
    }

    public String getFormattedDuration() {
        return NumberUtil.formatTime(getAudioTrack().getDuration());
    }

    public String getFormattedTotalTimeLeft(IPlayer player) {
        if (getAudioTrack().getInfo().isStream) {
            return "LIVE";
        }

        return NumberUtil.formatTime(getAudioTrack().getDuration() - player.getTrackPosition());
    }

    @Override
    public void setMetadata(@Nonnull String key, @Nullable Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        if (value == null) {
            metadata.remove(key);
        } else {
            metadata.put(key, value);
        }
    }

    @Override
    public void removeMetadata(@Nonnull String key) {
        if (metadata != null) {
            metadata.remove(key);
        }
    }

    @Nullable
    @Override
    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    @Nullable
    @Override
    public Object getMetadataFromKey(@Nonnull String key) {
        if (metadata == null) {
            return null;
        }
        return metadata.getOrDefault(key, null);
    }

    @Override
    public boolean hasMetadataKey(@Nonnull String key) {
        return metadata != null && metadata.containsKey(key);
    }

    public AudioTrackContainer makeClone() {
        AudioTrackContainer container = new AudioTrackContainer(audioTrack.makeClone(), requester);

        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                container.setMetadata(entry.getKey(), entry.getValue());
            }
        }

        return container;
    }
}
