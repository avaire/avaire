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

import java.util.ArrayList;
import java.util.List;

public class PluginHolder {

    private final String name;
    private final String description;
    private final List<String> authors;
    private final PluginRepository repository;

    /**
     * Creates a new plugin holder instance for the given JSON object
     * instance, the constructor will attempt to load the plugin
     * name, description, authors, and repo source.
     *
     * @param jsonObject The JSOn object instance the plugin holder
     *                   instance should be created from.
     */
    public PluginHolder(JSONObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.description = jsonObject.getString("description");
        this.repository = new PluginRepository(jsonObject);
        this.authors = new ArrayList<>();

        if (jsonObject.has("author")) {
            authors.add(jsonObject.getString("author"));
        }

        if (jsonObject.has("authors")) {
            for (Object author : jsonObject.getJSONArray("authors")) {
                authors.add((String) author);
            }
        }
    }

    /**
     * Gets the name of the plugin.
     *
     * @return The name of the plugin.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin description.
     *
     * @return The description of the plugin.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a list of the authors that created the plugin.
     *
     * @return The list of authors that created the plugin.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Gets the plugin source repository, this is defined as a source(GitHub,
     * BitBucket, GitLab, etc) and the repository it-self.
     *
     * @return The plugin source repository.
     */
    public PluginRepository getRepository() {
        return repository;
    }
}
