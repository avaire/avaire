package com.avairebot.contracts.config;

import com.avairebot.exceptions.FailedToLoadPropertiesConfigurationException;

import java.io.IOException;
import java.util.Properties;

public abstract class PropertyConfiguration {

    protected final Properties properties = new Properties();

    protected void loadProperty(ClassLoader classLoader, String propertyFileName) {
        try {
            properties.load(
                classLoader.getResourceAsStream(propertyFileName)
            );
        } catch (IOException e) {
            throw new FailedToLoadPropertiesConfigurationException("Failed to load " + propertyFileName, e);
        }
    }
}
