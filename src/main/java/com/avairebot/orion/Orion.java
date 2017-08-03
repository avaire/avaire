package com.avairebot.orion;

import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Orion implements EventListener {

    public final MainConfiguration config;

    public Orion() throws IOException {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            System.err.println("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        try {
            new JDABuilder(AccountType.BOT).setToken(this.config.botAuth().getToken()).addEventListener(this).buildAsync();
        } catch (LoginException | RateLimitedException e) {
            System.err.println("Something went wrong while trying to connect to Discord, exiting program...");
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void onEvent(Event e) {
        System.out.println(e.getClass().getName());
    }
}
