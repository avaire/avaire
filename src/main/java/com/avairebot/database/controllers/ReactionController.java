/*
 * Copyright (c) 2018.
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
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.ReactionTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReactionController {

    public static final Cache<Long, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(ReactionController.class);

    @CheckReturnValue
    public static Collection fetchReactions(AvaIre avaire, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }
        return fetchReactions(avaire, message.getGuild());
    }

    @CheckReturnValue
    public static Collection fetchReactions(AvaIre avaire, Guild guild) {
        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, guild.getIdLong(), () -> {
            log.debug("Guild Reaction cache for " + guild.getId() + " was refreshed");

            try {
                return avaire.getDatabase()
                    .newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                    .selectAll()
                    .where("guild_id", guild.getId())
                    .orderBy("message_id")
                    .get();
            } catch (Exception ex) {
                AvaIre.getLogger().error(ex.getMessage(), ex);
                return null;
            }
        });
    }

    @CheckReturnValue
    public static ReactionTransformer fetchReactionFromMessage(AvaIre avaire, Message message) {
        Collection reactions = fetchReactions(avaire, message);
        if (reactions == null) {
            return null;
        }

        List<DataRow> reaction = reactions.where("message_id", message.getIdLong());
        if (reaction.isEmpty()) {
            return null;
        }

        return new ReactionTransformer(reaction.get(0));
    }

    public static void forgetCache(long guildId) {
        cache.invalidate(guildId);
    }
}
