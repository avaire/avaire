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
