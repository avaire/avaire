/*
 * Copyright (c) 2020.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */
package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.transformers.EconomyTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EconomyController
{
    public static final Cache<String, EconomyTransformer> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(45, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(EconomyController.class);

    public static void updateUserClaims(AvaIre avaire, long guidId, long userId, int claims)
    {
        try
        {
            avaire.getDatabase().newQueryBuilder(Constants.ECONOMY_TABLE_NAME)
                .where("guild_id",guidId)
                .andWhere("user_id",userId)
                .update(statement ->
                {
                    statement.set("timesClaimed",claims);
                });
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }
    }

    public static void resetDailyClaims(AvaIre avaire)
    {
        try
        {
            avaire.getDatabase().newQueryBuilder(Constants.ECONOMY_TABLE_NAME)
                .update(statement ->
                {
                    statement.set("timesClaimed",0);
                });
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }
    }


    @CheckReturnValue
    public static EconomyTransformer fetchEconomy( AvaIre avaire, Message message, long user) {
        if (!message.getChannelType().isGuild())
        {
            return null;
        }



        return (EconomyTransformer) CacheUtil.getUncheckedUnwrapped(cache, asKey(message.getGuild(),user), () ->
        {
            try
            {
                EconomyTransformer transformer = new EconomyTransformer(
                    avaire.getDatabase()
                        .newQueryBuilder(Constants.ECONOMY_TABLE_NAME)
                        .select("economy.user_id","economy.guild_id","economy.balance","economy.dailyClaims",
                            "experiences.username","experiences.discriminator","experiences.avatar")
                        .where("economy.user_id", user)
                        .andWhere("economy.guild_id", message.getGuild().getId())
                        .innerJoin(
                            Constants.PLAYER_EXPERIENCE_TABLE_NAME, Constants.ECONOMY_TABLE_NAME + "." + "user_id",
                            Constants.PLAYER_EXPERIENCE_TABLE_NAME + "." + "user_id")
                        .get().first()
                );


                if (!transformer.hasData())
                {
                    transformer.setBalance(100);

                    avaire.getDatabase().newQueryBuilder(Constants.ECONOMY_TABLE_NAME)
                        .insert(statement -> {
                            statement.set("guild_id", message.getGuild().getId())
                                .set("user_id", user)
                                .set("balance", 100);
                        });
                    return transformer;
                }

                if (!transformer.isActive())
                {
                    avaire.getDatabase()
                        .newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                        .where("user_id", user)
                        .andWhere("guild_id", message.getGuild().getId())
                        .update(statement -> {
                            statement.set("active", true);
                        });
                }

                return transformer;

            } catch (Exception ex)
            {
                ex.printStackTrace();
                AvaIre.getLogger().error(ex.getMessage(), ex);
                return null;
            }
        });
    }

    private static String asKey(@Nonnull Guild guild, @Nonnull long user) {
        return guild.getId() + ":" + user;
    }


    public static void forgetCache(long userId) {
        List<String> toRemove = new ArrayList<>();
        for (String key : cache.asMap().keySet()) {
            if (key.endsWith(":" + userId)) {
                toRemove.add(key);
            }
        }

        if (!toRemove.isEmpty()) {
            cache.invalidateAll(toRemove);
        }
    }

    public static void forgetCacheForGuild(long guildId) {
        List<String> toRemove = new ArrayList<>();
        for (String key : cache.asMap().keySet()) {
            if (key.startsWith(guildId + ":")) {
                toRemove.add(key);
            }
        }

        if (!toRemove.isEmpty()) {
            cache.invalidateAll(toRemove);
        }
    }
}
