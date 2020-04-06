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

package com.avairebot.audio.searcher;

import com.avairebot.contracts.toggle.Feature;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum SearchProvider implements Feature {

    YOUTUBE(1, "ytsearch:", "youtube.com", "youtu.be"),
    SOUNDCLOUD(2, "scsearch:", "soundcloud.com"),
    LOCAL(3),
    URL(4);

    private static final SearchProvider DEFAULT_PROVIDER = URL;

    private final int id;
    private final String prefix;
    private final String[] domains;

    SearchProvider(int id) {
        this(id, null);
    }

    SearchProvider(int id, String prefix, String... domains) {
        this.id = id;
        this.prefix = prefix;
        this.domains = domains;
    }

    /**
     * Gets the search provider matching the source of the given audio track,
     * if no search providers matches were found the {@link #URL url search
     * provider} will be returned instead.
     *
     * @param track The audio track the search provider should be loaded for.
     * @return The matching search provider for the given audio track,
     *         or the {@link #URL url search provider}.
     */
    @Nonnull
    public static SearchProvider fromTrack(@Nonnull AudioTrack track) {
        String trackUrl = track.getInfo().uri;
        for (SearchProvider provider : values()) {
            if (provider.getDomains() != null && provider.matchesDomain(trackUrl)) {
                return provider;
            }
        }
        return DEFAULT_PROVIDER;
    }

    /**
     * Gets the matching search provider by name.
     *
     * @param name The name of the search provider that should be returned.
     * @return The matching search provider for the given name,
     *         or {@code NULL} if there were no match.
     */
    @Nullable
    public static SearchProvider fromName(String name) {
        for (SearchProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Gets the matching search provider by its ID.
     *
     * @param id The ID of the search provider that should be returned.
     * @return The matching search provider for the given ID,
     *         or {@code NULL} if there were no match.
     */
    @Nullable
    public static SearchProvider fromId(int id) {
        for (SearchProvider provider : values()) {
            if (provider.getId() == id) {
                return provider;
            }
        }
        return null;
    }

    /**
     * The unique ID of the search provider, the ID is used to represent cached
     * search results stored in the database, along with the query used
     * to fetch the original search result in the first place.
     *
     * @return The ID of the search provider.
     */
    public int getId() {
        return id;
    }

    /**
     * The searchable prefix used by LavaPlayer to search
     * for tracks using the current provider.
     *
     * @return The prefix used for search in LavaPlayer, or {@code NULL} if
     *         the current instance is not a searchable search provider.
     */
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    /**
     * The array of domains that is used by the current search provider,
     * if the search provider doesn't have any domains liked to it,
     * it will just return an empty string array instead.
     *
     * @return The array of domains used by the search providers.
     */
    public String[] getDomains() {
        return domains;
    }

    /**
     * Checks if the given string matches any of the domains for the current
     * search provider, the string is matched to make sure it just contains
     * the words for the domain in the string, not that the entire string
     * matches one of the domains.
     *
     * @param string The string that should be matched against the domains.
     * @return {@code True} if the given string contains one of the domains
     *         for the current provider, {@code False} otherwise.
     */
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

    /**
     * Checks if the current search provider is searchable or not.
     *
     * @return {@code True} if the search provider is searchable,
     *         {@code False} otherwise.
     */
    public boolean isSearchable() {
        return getPrefix() != null
            && getDomains() != null;
    }
}
