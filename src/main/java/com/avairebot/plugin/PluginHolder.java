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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public PluginRepository getRepository() {
        return repository;
    }
}
