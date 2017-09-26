package com.avairebot.orion.database.controllers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.google.gson.Gson;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildController {

    private static final String CACHE_STRING = "database.guilds.%s";

    public static GuildTransformer fetchGuild(Orion orion, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        if (isCached(orion, message.getGuild().getId())) {
            return (GuildTransformer) orion.cache.getAdapter(CacheType.MEMORY).get(
                    String.format(CACHE_STRING, message.getGuild().getId())
            );
        }

        try {
            GuildTransformer transformer = new GuildTransformer(orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", message.getGuild().getId())
                    .get().first());

            if (!transformer.hasData()) {
                final String cacheToken = String.format(CACHE_STRING, message.getGuild().getId());
                try {
                    orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                            .insert(statement -> {
                                statement.set("id", message.getGuild().getId())
                                        .set("owner", message.getGuild().getOwner().getUser().getId())
                                        .set("name", message.getGuild().getName())
                                        .set("channels_data", buildChannelData(message.getGuild().getTextChannels()));

                                if (message.getGuild().getIconId() != null) {
                                    statement.set("icon", message.getGuild().getIconId());
                                }

                                orion.cache.getAdapter(CacheType.MEMORY)
                                        .put(cacheToken, new GuildTransformer(new DataRow(statement.getItems())), 2);
                            });
                } catch (Exception ex) {
                    orion.logger.fatal(ex);
                }

                return (GuildTransformer) orion.cache.getAdapter(CacheType.MEMORY).get(cacheToken);
            }

            orion.cache.getAdapter(CacheType.MEMORY).put(String.format(CACHE_STRING, message.getGuild().getId()), transformer, 300);

            return transformer;
        } catch (SQLException ex) {
            orion.logger.fatal(ex);
            return null;
        }
    }

    private static boolean isCached(Orion orion, String guildId) {
        return orion.cache.getAdapter(CacheType.MEMORY).has(
                String.format(CACHE_STRING, guildId)
        );
    }

    private static String buildChannelData(List<TextChannel> textChannels) {
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
