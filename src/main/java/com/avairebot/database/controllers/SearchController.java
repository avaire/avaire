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
import com.avairebot.audio.TrackRequestContext;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.transformers.PurchasesTransformer;
import com.avairebot.database.transformers.SearchResultTransformer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SearchController {

    public static final Cache<Long, PurchasesTransformer> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    public static SearchResultTransformer fetchSearchResult(TrackRequestContext context) {
        try {
            Collection result = AvaIre.getInstance().getDatabase().query(
                createSearchQueryFromContext(context)
            );

            if (result.isEmpty()) {
                return null;
            }

            return new SearchResultTransformer(result.first());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cacheSearchResult(TrackRequestContext context, AudioPlaylist playlist) {
        try {
            AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME)
                .useAsync(true)
                .insert(statement -> {
                    statement.set("provider", context.getProvider().getId());
                    statement.set("query", context.getProvider().isSearchable()
                        ? context.getQuery().toLowerCase().trim()
                        : context.getQuery()
                    );
                    statement.set("result", new SearchResultTransformer.SerializableAudioPlaylist(playlist).toString(), true);
                });
        } catch (SQLException e) {
            // This will never be thrown since we're using an Async query.
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static String createSearchQueryFromContext(TrackRequestContext context) throws SQLException {
        String base = AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME)
            .where("provider", context.getProvider().getId())
            .toSQL();

        String query = StringUtils.chop(base) +
            " AND `query` = ?;";

        try (PreparedStatement statement = AvaIre.getInstance().getDatabase().getConnection().getConnection().prepareStatement(query)) {
            statement.setString(1, context.getProvider().isSearchable()
                ? context.getQuery().toLowerCase().trim()
                : context.getQuery()
            );

            String[] parts = statement.toString().split(" ");

            return String.join(" ", Arrays.copyOfRange(
                parts, 1, parts.length
            ));
        }
    }
}
