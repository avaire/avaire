package com.avairebot.orion.database.controllers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.CheckReturnValue;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildController {

    private static final String CACHE_STRING = "database.guilds.%s";

    @CheckReturnValue
    public static GuildTransformer fetchGuild(Orion orion, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return fetchGuild(orion, message.getGuild());
    }

    @CheckReturnValue
    public static GuildTransformer fetchGuild(Orion orion, Guild guild) {
        if (isCached(orion, guild.getId())) {
            return (GuildTransformer) orion.getCache().getAdapter(CacheType.MEMORY).get(
                String.format(CACHE_STRING, guild.getId())
            );
        }

        try {
            GuildTransformer transformer = new GuildTransformer(orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .select("guild_types.name as type_name", "guild_types.limits as type_limits", "guilds.*")
                .leftJoin("guild_types", "guilds.type", "guild_types.id")
                .where("guilds.id", guild.getId())
                .get().first());

            if (!transformer.hasData()) {
                final String cacheToken = String.format(CACHE_STRING, guild.getId());
                try {
                    orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                        .insert(statement -> {
                            statement.set("id", guild.getId())
                                .set("owner", guild.getOwner().getUser().getId())
                                .set("name", guild.getName())
                                .set("channels_data", buildChannelData(guild.getTextChannels()));

                            if (guild.getIconId() != null) {
                                statement.set("icon", guild.getIconId());
                            }

                            orion.getCache().getAdapter(CacheType.MEMORY)
                                .put(cacheToken, new GuildTransformer(new DataRow(statement.getItems())), 2);
                        });
                } catch (Exception ex) {
                    Orion.getLogger().error(ex.getMessage(), ex);
                }

                return (GuildTransformer) orion.getCache().getAdapter(CacheType.MEMORY).get(cacheToken);
            }

            orion.getCache().getAdapter(CacheType.MEMORY).put(String.format(CACHE_STRING, guild.getId()), transformer, 300);

            return transformer;
        } catch (SQLException ex) {
            Orion.getLogger().error(ex.getMessage(), ex);
            return null;
        }
    }

    private static boolean isCached(Orion orion, String guildId) {
        return orion.getCache().getAdapter(CacheType.MEMORY).has(
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
        return Orion.GSON.toJson(channels);
    }
}
