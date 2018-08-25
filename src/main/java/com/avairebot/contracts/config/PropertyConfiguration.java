package com.avairebot.contracts.config;

import com.avairebot.exceptions.FailedToLoadPropertiesConfigurationException;

import java.io.IOException;
import java.util.Properties;

public abstract class PropertyConfiguration {

    protected final Properties properties = new Properties();

    /**
     * Loads the properties file using the given class loader and sets the
     * value to the {@link PropertyConfiguration#properties properties} object.
     *
     * @param classLoader      The class loader that should be used to load the properties file.
     * @param propertyFileName The name of the properties file that should be loaded.
     */
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
