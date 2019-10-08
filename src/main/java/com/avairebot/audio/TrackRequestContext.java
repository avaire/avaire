/*
 * Copyright (c) 2019.
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

import com.avairebot.audio.seracher.SearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nonnull;

public class TrackRequestContext {

    private final String query;
    private SearchProvider provider;

    public TrackRequestContext(String query, SearchProvider provider) {
        this.query = query;
        this.provider = provider;
    }

    public TrackRequestContext(@Nonnull AudioTrack track) {
        query = track.getInfo().uri;
        provider = SearchProvider.fromTrack(track);
    }

    public String getFormattedQuery() {
        if (provider.getPrefix() == null) {
            return query;
        }
        return provider.getPrefix() + query;
    }

    public String getQuery() {
        return query;
    }

    public SearchProvider getProvider() {
        return provider;
    }

    public void setProvider(SearchProvider provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return getFormattedQuery();
    }
}
