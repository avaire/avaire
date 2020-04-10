package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.transformers.RemindersTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class RemindersController
{

    public static final Cache<Long, RemindersTransformer> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(90, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(RemindersController.class);

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
