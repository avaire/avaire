/*
 * Copyright (c) 2019.
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
import com.avairebot.database.transformers.RemindersTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class RemindersController {

    public static final Cache<Long, RemindersTransformer> cache = CacheBuilder.newBuilder()
                                                                      .recordStats()
                                                                      .expireAfterAccess(45, TimeUnit.MINUTES)
                                                                      .build();

    private static final Logger log = LoggerFactory.getLogger(RemindersController.class);

    @Nullable
    @CheckReturnValue
    public static RemindersTransformer fetchPendingReminders(User user) {
        if (user == null || !user.isBot())
        {
            return null;
        }
        return fetchPendingReminders(user.getIdLong());
    }

    @Nonnull
    @CheckReturnValue
    public static RemindersTransformer fetchPendingReminders(long userId) {
        return (RemindersTransformer) CacheUtil.getUncheckedUnwrapped(cache, userId, () -> {
            log.debug("Reminders cache for {} was refreshed", userId);
            try
            {
                return new RemindersTransformer(AvaIre.getInstance().getDatabase()
                                                    .newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                                                    .selectAll()
                                                    .where("user_id", userId)
                                                    .andWhere("sent", false)
                                                    .get());
            } catch (SQLException e)
            {
                log.error("Failed to get reminders for user {}, error: {}", userId, e.getMessage(), e);

                return new RemindersTransformer(null);
            }
        });
    }


}
