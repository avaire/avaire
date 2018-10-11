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

package com.avairebot.plugin;

import java.util.regex.Matcher;

public enum PluginSource {

    GITHUB("GitHub", "https://github.com/:name", "https://api.github.com/repos/:name/releases");

    private final String name;
    private final String sourceUrl;
    private final String releasesUrl;

    PluginSource(String name, String sourceUrl, String releasesUrl) {
        this.name = name;
        this.sourceUrl = sourceUrl;
        this.releasesUrl = releasesUrl;
    }

    public static PluginSource fromName(String name) {
        for (PluginSource source : values()) {
            if (source.getName().equalsIgnoreCase(name)) {
                return source;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getSourceUrl(String name) {
        return buildUrl(sourceUrl, name);
    }

    public String getReleasesUrl(String name) {
        return buildUrl(releasesUrl, name);
    }

    private String buildUrl(String url, String name) {
        return url.replace(":name", Matcher.quoteReplacement(name));
    }
}
