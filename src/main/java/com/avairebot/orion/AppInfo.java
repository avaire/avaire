package com.avairebot.orion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppInfo {

    private static AppInfo INSTANCE;
    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);

    public final String VERSION;
    public final String GROUP_ID;
    public final String ARTIFACT_ID;
    public final String BUILD_NUMBER;

    private AppInfo() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/orion.properties");
        Properties prop = new Properties();

        try {
            prop.load(resourceAsStream);
        } catch (IOException e) {
            log.error("Failed to load app.properties", e);
        }

        this.VERSION = prop.getProperty("version").split("_")[0];
        this.BUILD_NUMBER = prop.getProperty("version").split("_")[1];
        this.GROUP_ID = prop.getProperty("groupId");
        this.ARTIFACT_ID = prop.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (INSTANCE == null) {
            INSTANCE = new AppInfo();
        }
        return INSTANCE;
    }

    public String getVersionBuild() {
        return VERSION + "_" + BUILD_NUMBER;
    }
}
