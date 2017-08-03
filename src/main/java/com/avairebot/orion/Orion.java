package com.avairebot.orion;

import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.handlers.EventTypes;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Orion {

    public final MainConfiguration config;

    public Orion() throws IOException {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            System.err.println("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        try {
            this.prepareJDA().buildAsync();
        } catch (LoginException | RateLimitedException e) {
            System.err.println("Something went wrong while trying to connect to Discord, exiting program...");
            e.printStackTrace();
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
                System.err.println("Invalid listener adapter object parsed, failed to create a new instance!");
            } catch (IllegalAccessException ex) {
                System.err.printf("An attempt was made to register a event listener called %s but it failed somewhere!", event, ex);
            }
        }

        return builder;
    }
}
