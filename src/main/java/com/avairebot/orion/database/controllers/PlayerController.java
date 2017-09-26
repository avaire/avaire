package com.avairebot.orion.database.controllers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.PlayerTransformer;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;

public class PlayerController {

    private static final String CACHE_STRING = "database.player.%s.%s";

    public static PlayerTransformer fetchPlayer(Orion orion, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        final String cacheToken = String.format(CACHE_STRING,
                message.getGuild().getId(),
                message.getAuthor().getId()
        );

        if (orion.cache.getAdapter(CacheType.MEMORY).has(cacheToken)) {
            return (PlayerTransformer) orion.cache.getAdapter(CacheType.MEMORY).get(cacheToken);
        }

        try {
            PlayerTransformer transformer = new PlayerTransformer(orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .where("user_id", message.getAuthor().getId())
                    .andWhere("guild_id", message.getGuild().getId())
                    .get().first());

            if (!transformer.hasData()) {
                orion.database.newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                        .insert(statement -> {
                            statement.set("guild_id", message.getGuild().getId())
                                    .set("user_id", message.getAuthor().getId())
                                    .set("username", message.getAuthor().getName())
                                    .set("discriminator", message.getAuthor().getDiscriminator())
                                    .set("avatar", message.getAuthor().getAvatarId())
                                    .set("experience", 100);

                            orion.cache.getAdapter(CacheType.MEMORY).put(cacheToken, new PlayerTransformer(new DataRow(statement.getItems())), 2);
                        });

                return (PlayerTransformer) orion.cache.getAdapter(CacheType.MEMORY).get(cacheToken);
            }

            orion.cache.getAdapter(CacheType.MEMORY).put(cacheToken, transformer, 300);

            return transformer;
        } catch (SQLException ex) {
            orion.logger.fatal(ex);
            return null;
        }
    }
}
