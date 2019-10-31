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

    /**
     * Gets the plugin source from name, this check will ignore letter casing.
     *
     * @param name The name of the plugin source that should be returned.
     * @return The plugin source matching the given name, or
     *         {@code NULL} if there were not matches.
     */
    public static PluginSource fromName(String name) {
        for (PluginSource source : values()) {
            if (source.getName().equalsIgnoreCase(name)) {
                return source;
            }
        }
        return null;
    }

    /**
     * Gets the name of the plugin source.
     *
     * @return The name of the plugin source.
     */
    public String getName() {
        return name;
    }

    /**
     * Formats and returns the source URL using the given project name,
     * this is usually the git repository name, followed by a forward
     * slash, and then the actual project name.
     * <p>
     * Example: "avaire/avaire", "senither/meow-api/", etc.
     *
     * @param name The project name that should be formatted into the source URL.
     * @return
     */
    public String getSourceUrl(String name) {
        return buildUrl(sourceUrl, name);
    }

    /**
     * Formats and returns the release URL using the given project name,
     * this is usually the git repository name, followed by a forward
     * slash, and then the actual project name.
     * <p>
     * Example: "avaire/avaire", "senither/meow-api/", etc.
     *
     * @param name The project name that should be formatted into the release URL.
     * @return
     */
    public String getReleasesUrl(String name) {
        return buildUrl(releasesUrl, name);
    }

    /**
     * Builds the URL type with the given project name.
     *
     * @param url  The URL that should be formatted and built.
     * @param name The git project name that should be formatted into the URL.
     * @return The formatted URL.
     */
    private String buildUrl(String url, String name) {
        return url.replace(":name", Matcher.quoteReplacement(name));
    }
}
