package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.query.ChangeableStatement;
import com.avairebot.database.transformers.GuildTransformer;
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
    public static GuildTransformer fetchGuild(AvaIre avaire, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return fetchGuild(avaire, message.getGuild());
    }

    @CheckReturnValue
    public static GuildTransformer fetchGuild(AvaIre avaire, Guild guild) {
        if (isCached(avaire, guild.getId())) {
            return (GuildTransformer) avaire.getCache().getAdapter(CacheType.MEMORY).get(
                String.format(CACHE_STRING, guild.getId())
            );
        }

        try {
            GuildTransformer transformer = new GuildTransformer(avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .select("guild_types.name as type_name", "guild_types.limits as type_limits", "guilds.*")
                .leftJoin("guild_types", "guilds.type", "guild_types.id")
                .where("guilds.id", guild.getId())
                .get().first());

            if (!transformer.hasData()) {
                final String cacheToken = String.format(CACHE_STRING, guild.getId());
                try {
                    avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                        .insert(statement -> {
                            statement.set("id", guild.getId())
                                .set("owner", guild.getOwner().getUser().getId())
                                .set("name", guild.getName(), true)
                                .set("channels_data", buildChannelData(guild.getTextChannels()), true);

                            if (guild.getIconId() != null) {
                                statement.set("icon", guild.getIconId());
                            }

                            avaire.getCache().getAdapter(CacheType.MEMORY)
                                .put(cacheToken, new GuildTransformer(new DataRow(statement.getItems())), 300);
                        });
                } catch (Exception ex) {
                    AvaIre.getLogger().error(ex.getMessage(), ex);
                }

                return (GuildTransformer) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken);
            }

            // If the guild haven't been encoded yet, we'll do it below.
            String name = transformer.getRawData().get("name").toString();
            if (name.startsWith("base64:")) {
                avaire.getCache()
                    .getAdapter(CacheType.MEMORY)
                    .put(String.format(CACHE_STRING, guild.getId()), transformer, 300);

                return transformer;
            }

            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", guild.getId())
                .update(statement -> {
                    statement.set("name", guild.getName(), true);
                    statement.set("channels_data", buildChannelData(guild.getTextChannels()), true);

                    setUpdateStatementFor(transformer, statement, "channels");
                    setUpdateStatementFor(transformer, statement, "claimable_roles");
                    setUpdateStatementFor(transformer, statement, "prefixes");
                    setUpdateStatementFor(transformer, statement, "aliases");
                });

            return transformer;
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);
            return null;
        }
    }

    private static boolean isCached(AvaIre avaire, String guildId) {
        return avaire.getCache().getAdapter(CacheType.MEMORY).has(
            String.format(CACHE_STRING, guildId)
        );
    }

    public static boolean forgetCache(AvaIre avaire, String guildId) {
        if (!isCached(avaire, guildId)) {
            return false;
        }

        return avaire.getCache().getAdapter(CacheType.MEMORY).forget(
            String.format(CACHE_STRING, guildId)
        ) != null;
    }

    public static String buildChannelData(List<TextChannel> textChannels) {
        List<Map<String, Object>> channels = new ArrayList<>();
        for (TextChannel channel : textChannels) {
            Map<String, Object> item = new HashMap<>();

            item.put("id", channel.getId());
            item.put("name", channel.getName());
            item.put("position", channel.getPosition());

            channels.add(item);
        }
        return AvaIre.GSON.toJson(channels);
    }

    private static void setUpdateStatementFor(Transformer transformer, ChangeableStatement statement, String name) {
        if (transformer.getRawData().get(name, null) != null) {
            statement.set(name, transformer.getRawData().getString(name), true);
        }
    }
}
