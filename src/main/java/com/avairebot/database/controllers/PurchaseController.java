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
import com.avairebot.database.transformers.PurchasesTransformer;
import com.avairebot.language.I18n;
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

public class PurchaseController {

    public static final Cache<Long, PurchasesTransformer> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(45, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    private static final PurchasesTransformer emptyPurchases = new PurchasesTransformer(null);
    private static final String[] requiredPurchasesColumns = new String[]{
        "`type`", "`type_id`"
    };

    @Nullable
    @CheckReturnValue
    public static PurchasesTransformer fetchPurchases(User user) {
        if (user == null || !user.isBot()) {
            return null;
        }
        return fetchPurchases(user.getIdLong());
    }

    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("ConstantConditions")
    public static PurchasesTransformer fetchPurchases(long userId) {
        return (PurchasesTransformer) CacheUtil.getUncheckedUnwrapped(cache, userId, () -> {
            log.debug("Purchase cache for {} was refreshed", userId);

            try {
                int index = 0;
                String[] purchaseColumns = new String[requiredPurchasesColumns.length + 1];
                for (String value : requiredPurchasesColumns) {
                    purchaseColumns[index++] = value;
                }

                String selectQuery = AvaIre.getInstance().getDatabase()
                    .newQueryBuilder(Constants.VOTES_TABLE_NAME)
                    .select("selected_bg")
                    .where("user_id", userId)
                    .toSQL();

                purchaseColumns[index] = I18n.format("({0}) as 'selected'",
                    selectQuery.substring(0, selectQuery.length() - 1)
                );

                return new PurchasesTransformer(
                    AvaIre.getInstance().getDatabase()
                        .newQueryBuilder(Constants.PURCHASES_TABLE_NAME)
                        .selectRaw(String.join(", ", purchaseColumns))
                        .where("user_id", userId)
                        .get()
                );
            } catch (SQLException e) {
                log.error("Failed to get purchases for user {}, error: {}", userId, e.getMessage(), e);

                return getEmptyPurchases();
            }
        });
    }

    public static void forgetCache(long userId) {
        cache.invalidate(userId);
    }

    public static PurchasesTransformer getEmptyPurchases() {
        return emptyPurchases;
    }
}
