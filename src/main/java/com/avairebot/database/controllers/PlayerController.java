package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.utilities.RandomUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.CheckReturnValue;
import java.sql.SQLException;

public class PlayerController {

    private static final String CACHE_STRING = "database.player.%s.%s";

    @CheckReturnValue
    public static PlayerTransformer fetchPlayer(AvaIre avaire, Message message) {
        return fetchPlayer(avaire, message, message.getAuthor());
    }

    @CheckReturnValue
    public static PlayerTransformer fetchPlayer(AvaIre avaire, Message message, User user) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        final String cacheToken = String.format(CACHE_STRING,
            message.getGuild().getId(),
            user.getId()
        );

        if (avaire.getCache().getAdapter(CacheType.MEMORY).has(cacheToken)) {
            return (PlayerTransformer) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken);
        }

        try {
            PlayerTransformer transformer = new PlayerTransformer(avaire.getDatabase()
                .newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("user_id", user.getId())
                .andWhere("guild_id", message.getGuild().getId())
                .get().first());

            if (!transformer.hasData()) {
                avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("guild_id", message.getGuild().getId())
                            .set("user_id", user.getId())
                            .set("username", user.getName(), true)
                            .set("discriminator", user.getDiscriminator())
                            .set("avatar", user.getAvatarId())
                            .set("experience", 100);

                        avaire.getCache()
                            .getAdapter(CacheType.MEMORY)
                            .put(cacheToken, new PlayerTransformer(
                                new DataRow(statement.getItems())
                            ), RandomUtil.getInteger(120) + 300);
                    });

                return (PlayerTransformer) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken);
            }

            if (isChanged(user, transformer)) {
                transformer.setUsername(user.getName());
                transformer.setDiscriminator(user.getDiscriminator());
                transformer.setAvatar(user.getAvatarId());

                updateUserData(avaire, user);
                avaire.getCache()
                    .getAdapter(CacheType.MEMORY)
                    .put(cacheToken, transformer, 300);

                return transformer;
            }

            // If the users name haven't been encoded yet, we'll do it below.
            String username = transformer.getUsernameRaw();
            if (username.startsWith("base64:")) {
                avaire.getCache()
                    .getAdapter(CacheType.MEMORY)
                    .put(cacheToken, transformer, RandomUtil.getInteger(120) + 300);

                return transformer;
            }

            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .useAsync(true)
                .where("user_id", message.getAuthor().getId())
                .update(statement -> statement.set("username", message.getAuthor().getName(), true));

            return transformer;
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);
            return null;
        }
    }

    public static void updateUserData(AvaIre avaire, User user) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .useAsync(true)
                .where("user_id", user.getId())
                .update(statement -> {
                    statement.set("username", user.getName(), true);
                    statement.set("discriminator", user.getDiscriminator());
                    statement.set("avatar", user.getAvatarId());
                });
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to update user with an ID of " + user.getId(), e);
        }
    }

    private static boolean isChanged(User user, PlayerTransformer transformer) {
        return !user.equals(transformer)
            || !user.getDiscriminator().equals(transformer.getDiscriminator())
            || !user.getAvatarId().equals(transformer.getAvatar());
    }
}
