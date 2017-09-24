package com.avairebot.orion.database.controllers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PlayerController {

    private static final String CACHE_STRING = "database.player.%s.%s";

    public static PlayerTransformer fetchPlayer(Orion orion, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        if (isCached(orion, message.getGuild().getId(), message.getAuthor().getId())) {
            return (PlayerTransformer) orion.cache.getAdapter(CacheType.MEMORY).get(
                    String.format(CACHE_STRING, message.getGuild().getId(), message.getAuthor().getId())
            );
        }

        try {
            PlayerTransformer transformer = new PlayerTransformer(orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectAll()
                    .where("user_id", message.getAuthor().getId())
                    .andWhere("guild_id", message.getGuild().getId())
                    .get().first());

            if (!transformer.hasData()) {
                Map<String, Object> items = new HashMap<>();
                items.put("guild_id", message.getGuild().getId());
                items.put("user_id", message.getAuthor().getId());
                items.put("username", message.getAuthor().getName());
                items.put("discriminator", message.getAuthor().getDiscriminator());
                items.put("avatar", message.getAuthor().getAvatarId());
                items.put("experience", 0);

                transformer = new PlayerTransformer(new DataRow(items));
                orion.cache.getAdapter(CacheType.MEMORY).put(String.format(CACHE_STRING,
                        message.getGuild().getId(),
                        message.getAuthor().getId()
                ), transformer, 2);

                try {
                    orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME).insert(items);
                } catch (Exception ex) {
                    orion.logger.fatal(ex);
                }

                return transformer;
            }

            orion.cache.getAdapter(CacheType.MEMORY).put(String.format(CACHE_STRING,
                    message.getGuild().getId(),
                    message.getAuthor().getId()
            ), transformer, 300);

            return transformer;
        } catch (SQLException ex) {
            orion.logger.fatal(ex);
            return null;
        }
    }

    private static boolean isCached(Orion orion, String guildId, String playerId) {
        return orion.cache.getAdapter(CacheType.MEMORY).has(
                String.format(CACHE_STRING, guildId, playerId)
        );
    }
}
