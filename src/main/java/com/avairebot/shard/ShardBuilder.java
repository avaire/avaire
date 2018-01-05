package com.avairebot.shard;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.handlers.EventTypes;
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
    protected synchronized static JDABuilder getDefaultShardBuilder(AvaIre avaire) {
        if (defaultShardBuilder == null) {
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                .setToken(avaire.getConfig().getString("discord.token"))
                .setGame(Game.watching("my code start up..."))
                .setReconnectQueue(avaire.getConnectQueue())
                .setAutoReconnect(true);

            Class[] eventArguments = new Class[1];
            eventArguments[0] = AvaIre.class;

            for (EventTypes event : EventTypes.values()) {
                try {
                    Object instance = event.getInstance().getDeclaredConstructor(eventArguments).newInstance(avaire);

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
