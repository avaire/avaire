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

package com.avairebot.contracts.plugin;

import com.avairebot.plugin.PluginRepository;

import java.util.List;

public interface Translator {

    /**
     * Gets the name of the plugin.
     *
     * @return The name of the plugin.
     */
    String getName();

    /**
     * Gets the description of the plugin.
     *
     * @return The description of the plugin.
     */
    String getDescription();

    /**
     * Gets the list of authors who created the plugin.
     *
     * @return The list of authros who created the plugin.
     */
    List<String> getAuthors();

    /**
     * Gets the plugin repository object for communicating with the plugins repository.
     *
     * @return The plugin repository object.
     */
    PluginRepository getRepository();

    /**
     * Checks if the plugin has been installed or not.
     *
     * @return <code>True</code> if the plugin has been installed, <code>False</code> otherwise.
     */
    boolean isInstalled();
}
