package com.avairebot.orion;

import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;

import java.io.IOException;

public class Orion {

    public final MainConfiguration config;

    public Orion() throws IOException {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            System.out.println("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }
    }
}
