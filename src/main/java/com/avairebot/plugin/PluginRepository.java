package com.avairebot.plugin;

import org.json.JSONObject;

public class PluginRepository {

    private final String repository;
    private final PluginSource source;

    public PluginRepository(JSONObject jsonObject) {
        this.repository = jsonObject.getString("repository");
        this.source = PluginSource.fromName(jsonObject.getString("source"));
    }

    public String getRepository() {
        return repository;
    }

    public PluginSource getSource() {
        return source;
    }
}
