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
