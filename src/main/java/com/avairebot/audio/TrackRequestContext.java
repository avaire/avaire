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

import com.avairebot.audio.searcher.SearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class TrackRequestContext {

    private static final Pattern punctuationRegex = Pattern.compile("[.,/#!$%^&*;:{}=\\-_`~()\"\']");

    private final String query;
    private SearchProvider provider;

    public TrackRequestContext(@Nonnull String query, @Nonnull SearchProvider provider) {
        this.query = query;
        this.provider = provider;
    }

    public TrackRequestContext(@Nonnull AudioTrack track) {
        query = track.getInfo().uri;
        provider = SearchProvider.fromTrack(track);
    }

    public String getFullQueryString() {
        if (provider.getPrefix() == null) {
            return getFormattedQuery();
        }

        return provider.getPrefix() + getFormattedQuery();
    }

    public String getFormattedQuery() {
        if (!provider.isSearchable()) {
            return query;
        }
        return punctuationRegex.matcher(query)
            .replaceAll("")
            .toLowerCase();
    }

    public String getQuery() {
        return query;
    }

    public SearchProvider getProvider() {
        return provider;
    }

    public void setProvider(@Nonnull SearchProvider provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return getFullQueryString();
    }
}
