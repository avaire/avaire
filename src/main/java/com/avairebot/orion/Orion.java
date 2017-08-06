package com.avairebot.orion;

import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.utility.InviteCommand;
import com.avairebot.orion.commands.utility.PingCommand;
import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.handlers.EventHandler;
import com.avairebot.orion.handlers.EventTypes;
import com.avairebot.orion.logger.Logger;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Orion {

    public final MainConfiguration config;
    public final Logger logger;

    public Orion() throws IOException {
        this.logger = new Logger(this);

        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            this.logger.error("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        this.registerCommands();

        try {
            this.prepareJDA().buildAsync();
        } catch (LoginException | RateLimitedException ex) {
            this.logger.error("Something went wrong while trying to connect to Discord, exiting program...");
            this.logger.exception(ex);
            System.exit(0);
        }
    }

    private void registerCommands() {
        CommandHandler.register(new PingCommand(this));
        CommandHandler.register(new InviteCommand(this));
    }

    private JDABuilder prepareJDA() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(this.config.botAuth().getToken());

        Class[] eventArguments = new Class[1];
        eventArguments[0] = Orion.class;

        for (EventTypes event : EventTypes.values()) {
            try {
                Object instance = event.getInstance().getDeclaredConstructor(eventArguments).newInstance(this);

                if (instance instanceof EventHandler) {
                    builder.addEventListener(instance);
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                this.logger.error("Invalid listener adapter object parsed, failed to create a new instance!");
                this.logger.exception(ex);
            } catch (IllegalAccessException ex) {
                this.logger.error("An attempt was made to register a event listener called " + event + " but it failed somewhere!");
                this.logger.exception(ex);
            }
        }

        return builder;
    }
}
