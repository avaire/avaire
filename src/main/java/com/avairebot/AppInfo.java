package com.avairebot;

import com.avairebot.contracts.config.PropertyConfiguration;

public class AppInfo extends PropertyConfiguration {

    private static AppInfo INSTANCE;

    public final String VERSION;
    public final String GROUP_ID;
    public final String ARTIFACT_ID;

    private AppInfo() {
        loadProperty(getClass().getClassLoader(), "app.properties");

        this.VERSION = properties.getProperty("version");
        this.GROUP_ID = properties.getProperty("groupId");
        this.ARTIFACT_ID = properties.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (INSTANCE == null) {
            INSTANCE = new AppInfo();
        }
        return INSTANCE;
    }
}
