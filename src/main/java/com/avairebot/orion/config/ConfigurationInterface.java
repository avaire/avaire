package com.avairebot.orion.config;

import java.io.IOException;

public interface ConfigurationInterface {

    ConfigurationCastableInterface loadConfiguration(String fileName, Class<?> type) throws IOException;

    String defaultConfig(String name);
}
