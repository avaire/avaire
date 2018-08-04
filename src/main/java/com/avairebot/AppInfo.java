package com.avairebot;

import com.avairebot.contracts.config.PropertyConfiguration;

public class AppInfo extends PropertyConfiguration {

    private static AppInfo instance;

    public final String version;
    public final String groupId;
    public final String artifactId;

    private AppInfo() {
        loadProperty(getClass().getClassLoader(), "app.properties");

        this.version = properties.getProperty("version");
        this.groupId = properties.getProperty("groupId");
        this.artifactId = properties.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (instance == null) {
            instance = new AppInfo();
        }
        return instance;
    }
}
