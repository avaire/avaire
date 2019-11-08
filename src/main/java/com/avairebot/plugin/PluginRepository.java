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

import org.json.JSONObject;

public class PluginRepository {

    private final String repository;
    private final PluginSource source;

    /**
     * Creates a new plugin repository from the given JSON object instance,
     * this will pull the repository and source from the JSON instance.
     *
     * @param jsonObject The JSON instance that the plugin repository should be created from.
     */
    PluginRepository(JSONObject jsonObject) {
        this.repository = jsonObject.getString("repository");
        this.source = PluginSource.fromName(jsonObject.getString("source"));
    }

    /**
     * Gets the plugin repository name.
     *
     * @return The plugin repository name.
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Gets the plugin repository source type.
     *
     * @return The plugin repository source type instance.
     */
    public PluginSource getSource() {
        return source;
    }
}
