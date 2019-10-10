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

package com.avairebot.audio.seracher;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public enum SearchProvider {

    YOUTUBE("ytsearch:", "youtube.com"),
    SOUNDCLOUD("scsearch:", "soundcloud.com"),
    LOCAL,
    URL;

    private static final SearchProvider DEFAULT_PROVIDER = URL;

    private final String prefix;
    private final String url;

    SearchProvider() {
        this(null, null);
    }

    SearchProvider(String prefix, String url) {
        this.prefix = prefix;
        this.url = url;
    }

    public static SearchProvider fromTrack(AudioTrack track) {
        String trackUrl = track.getInfo().uri;
        for (SearchProvider provider : SearchProvider.values()) {
            if (provider.getUrl() != null && trackUrl.contains(provider.getUrl())) {
                return provider;
            }
        }
        return DEFAULT_PROVIDER;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSearchable() {
        return getPrefix() != null
            && getUrl() != null;
    }
}
