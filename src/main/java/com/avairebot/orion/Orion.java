package com.avairebot.orion;

import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.handlers.EventTypes;
import com.avairebot.orion.logger.Logger;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Orion {

    public final MainConfiguration config;
    public final Logger logger;

    public Orion() throws IOException {
        this.logger = new Logger();

        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            this.logger.severe("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        try {
            this.prepareJDA().buildAsync();
        } catch (LoginException | RateLimitedException ex) {
            this.logger.severe("Something went wrong while trying to connect to Discord, exiting program...");
            this.logger.exception(ex);
            System.exit(0);
        }
    }

    public JDABuilder prepareJDA() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(this.config.botAuth().getToken());

        for (EventTypes event : EventTypes.values()) {
            try {
                Object instance = event.getInstance().newInstance();

                if (instance instanceof ListenerAdapter) {
                    builder.addEventListener(instance);
                }
            } catch (InstantiationException ex) {
                this.logger.severe("Invalid listener adapter object parsed, failed to create a new instance!");
                this.logger.exception(ex);
            } catch (IllegalAccessException ex) {
                this.logger.severe("An attempt was made to register a event listener called " + event + " but it failed somewhere!");
                this.logger.exception(ex);
            }
        }

        return builder;
    }
}
