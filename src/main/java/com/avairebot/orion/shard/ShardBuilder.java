package com.avairebot.orion.shard;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.handlers.EventTypes;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class ShardBuilder {

    private static final Logger log = LoggerFactory.getLogger(ShardBuilder.class);
    private static JDABuilder defaultShardBuilder;

    @Nonnull
    protected synchronized static JDABuilder getDefaultShardBuilder(Orion orion) {
        if (defaultShardBuilder == null) {
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                .setToken(orion.getConfig().botAuth().getToken())
                .setGame(Game.of("Loading components..."))
                .setReconnectQueue(orion.getConnectQueue())
                .setAutoReconnect(true);

            Class[] eventArguments = new Class[1];
            eventArguments[0] = Orion.class;

            for (EventTypes event : EventTypes.values()) {
                try {
                    Object instance = event.getInstance().getDeclaredConstructor(eventArguments).newInstance(orion);

                    if (instance instanceof EventHandler) {
                        builder.addEventListener(instance);
                    }
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    log.error("Invalid listener adapter object parsed, failed to create a new instance!", ex);
                } catch (IllegalAccessException ex) {
                    log.error("An attempt was made to register a event listener called " + event + " but it failed somewhere!", ex);
                }
            }

            defaultShardBuilder = builder;
        }

        return defaultShardBuilder;
    }
}
