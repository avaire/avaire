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

package com.avairebot.contracts.plugin;

public interface PluginAsset {

    /**
     * Gets the name of the plugin asset.
     *
     * @return The name of the plugin asset.
     */
    String getName();

    /**
     * Generates a URL that can be used to download the plugin asset.
     *
     * @return A URL that can be used to download the plugin asset.
     */
    String getDownloadableUrl();
}
