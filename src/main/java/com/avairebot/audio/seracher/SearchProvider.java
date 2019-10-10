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

import com.avairebot.contracts.toggle.Feature;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;

public enum SearchProvider implements Feature {

    YOUTUBE("ytsearch:", "youtube.com", "youtu.be"),
    SOUNDCLOUD("scsearch:", "soundcloud.com"),
    LOCAL,
    URL;

    private static final SearchProvider DEFAULT_PROVIDER = URL;

    private final String prefix;
    private final String[] domains;

    SearchProvider() {
        this(null);
    }

    SearchProvider(String prefix, String... domains) {
        this.prefix = prefix;
        this.domains = domains;
    }

    public static SearchProvider fromTrack(AudioTrack track) {
        String trackUrl = track.getInfo().uri;
        for (SearchProvider provider : values()) {
            if (provider.getDomains() != null && provider.matchesDomain(trackUrl)) {
                return provider;
            }
        }
        return DEFAULT_PROVIDER;
    }

    @Nullable
    public static SearchProvider fromName(String name) {
        for (SearchProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        return null;
    }

    public String getPrefix() {
        return prefix;
    }

    public String[] getDomains() {
        return domains;
    }

    public boolean matchesDomain(String string) {
        if (string == null) {
            return false;
        }

        for (String domain : getDomains()) {
            if (string.toLowerCase().contains(domain)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSearchable() {
        return getPrefix() != null
            && getDomains() != null;
    }
}
