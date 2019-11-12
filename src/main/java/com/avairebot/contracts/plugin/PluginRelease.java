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

import java.util.List;

public interface PluginRelease {

    /**
     * Gets the name of the plugin release from the git release instance.
     *
     * @return The name of the plugin release from the git release instance.
     */
    String getName();

    /**
     * Gets the tag associated with the git release.
     *
     * @return The tag associated with the git release.
     */
    String getTag();

    /**
     * Gets the URL to the git release.
     *
     * @return The URL to the git release.
     */
    String getUrl();

    /**
     * Gets a list of assets associated with the git release.
     *
     * @return A list of assets/files associated with the git release.
     */
    List<PluginAsset> getAssets();
}
