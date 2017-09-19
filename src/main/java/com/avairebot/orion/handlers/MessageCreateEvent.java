package com.avairebot.orion.handlers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.middleware.MiddlewareStack;
import com.google.gson.Gson;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MessageCreateEvent extends EventHandler {

    public MessageCreateEvent(Orion orion) {
        super(orion);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Statistics.addMessage();

        if (event.getAuthor().isBot()) {
            return;
        }

        loadDatabasePropertiesIntoMemory(event).thenAccept(guild -> {
            CommandContainer container = CommandHandler.getCommand(event.getMessage());
            if (container != null) {
                Statistics.addCommands();

                if (!container.getCommand().isAllowedInDM() && !event.getChannelType().isGuild()) {
                    MessageFactory.makeWarning(event.getMessage(), ":warning: You can not use this command in direct messages!").queue();
                    return;
                }

                (new MiddlewareStack(orion, event.getMessage(), container)).next();
            }
        });
    }

    private CompletableFuture<GuildTransformer> loadDatabasePropertiesIntoMemory(final MessageReceivedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (!event.getChannelType().isGuild()) {
                return null;
            }

            if (orion.cache.getAdapter(CacheType.MEMORY).has("guilds." + event.getGuild().getId())) {
                return (GuildTransformer) orion.cache.getAdapter(CacheType.MEMORY).get("guilds." + event.getGuild().getId());
            }

            try {
                GuildTransformer transformer = new GuildTransformer(orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                        .selectAll()
                        .where("id", event.getGuild().getId())
                        .get().first());

                if (!transformer.hasData()) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("id", event.getGuild().getId());
                    items.put("owner", event.getGuild().getOwner().getUser().getId());
                    items.put("name", event.getGuild().getName());
                    items.put("icon", event.getGuild().getIconId());
                    items.put("channels_data", buildChannelData(event.getGuild().getTextChannels()));

                    transformer = new GuildTransformer(new DataRow(items));
                    orion.cache.getAdapter(CacheType.MEMORY).put("guilds." + event.getGuild().getId(), transformer, 2);

                    orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME).insert(items);

                    return transformer;
                }

                orion.cache.getAdapter(CacheType.MEMORY).put("guilds." + event.getGuild().getId(), transformer, 300);

                return transformer;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private String buildChannelData(List<TextChannel> textChannels) {
        List<Map<String, Object>> channels = new ArrayList<>();
        for (TextChannel channel : textChannels) {
            Map<String, Object> item = new HashMap<>();

            item.put("id", channel.getId());
            item.put("name", channel.getName());
            item.put("position", channel.getPosition());

            channels.add(item);
        }
        return new Gson().toJson(channels);
    }
}
