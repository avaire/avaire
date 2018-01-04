package com.avairebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppInfo {

    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);
    private static AppInfo INSTANCE;

    public final String VERSION;
    public final String GROUP_ID;
    public final String ARTIFACT_ID;

    private AppInfo() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/app.properties");
        Properties prop = new Properties();

        try {
            prop.load(resourceAsStream);
        } catch (IOException e) {
            log.error("Failed to load app.properties", e);
        }

        this.VERSION = prop.getProperty("version");
        this.GROUP_ID = prop.getProperty("groupId");
        this.ARTIFACT_ID = prop.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (INSTANCE == null) {
            INSTANCE = new AppInfo();
        }
        return INSTANCE;
    }
}
