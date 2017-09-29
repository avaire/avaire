package com.avairebot.orion.contracts.config;

import java.io.IOException;

public interface ConfigurationInterface {

    /**
     * Loads the configuration with the given file name and type.
     *
     * @param fileName The name of the config file that should be loaded.
     * @param type     The type of configuration the config file should be cast to.
     * @return The configurations castable interface value.
     * @throws IOException
     */
    CastableInterface load(String fileName, Class<?> type) throws IOException;

    /**
     * Gets the default configuration with the given name, this should only be called if the
     * actually configuration can't be found and the default is needed to write a new file.
     *
     * @param name The name of the config file that should be generated.
     * @return The content of the given file name.
     */
    String defaultConfig(String name);
}
