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
import com.avairebot.database.transformers.SearchResultTransformer;
import com.avairebot.time.Carbon;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SearchController {

    public static final Cache<String, SearchResultTransformer> cache = CacheBuilder.newBuilder()
        .recordStats()
        .maximumSize(500)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    public static SearchResultTransformer fetchSearchResult(TrackRequestContext context) {
        return fetchSearchResult(context, TimeUnit.HOURS.toMillis(48));
    }

    public static SearchResultTransformer fetchSearchResult(TrackRequestContext context, long maxCacheAgeInMilis) {
        SearchResultTransformer cacheResult = cache.getIfPresent(context.getFullQueryString());
        if (cacheResult != null) {
            log.debug("Search request for {} with the {} provider was loaded from in-memory cache.",
                context.getQuery(), context.getProvider()
            );

            return cacheResult;
        }

        try {
            Collection result = AvaIre.getInstance().getDatabase().query(
                createSearchQueryFromContext(context, maxCacheAgeInMilis)
            );

            if (result.isEmpty()) {
                return null;
            }

            SearchResultTransformer resultTransformer = new SearchResultTransformer(result.first());

            cache.put(context.getFullQueryString(), resultTransformer);

            log.debug("Search request for {} with the {} provider was loaded from database cache.",
                context.getQuery(), context.getProvider()
            );

            return resultTransformer;
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
                    statement.set("created_at", Carbon.now());
                });
        } catch (SQLException e) {
            // This will never be thrown since we're using an Async query.
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static String createSearchQueryFromContext(TrackRequestContext context, long maxCacheAgeInMilis) throws SQLException {
        String base = AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME)
            .where("provider", context.getProvider().getId())
            .toSQL();

        String query = StringUtils.chop(base) +
            " AND `query` = ? AND `created_at` > ?;";

        try (PreparedStatement statement = AvaIre.getInstance().getDatabase().getConnection().getConnection().prepareStatement(query)) {
            statement.setString(1, context.getProvider().isSearchable()
                ? context.getQuery().toLowerCase().trim()
                : context.getQuery()
            );

            statement.setTimestamp(2, new Timestamp(
                (Carbon.now().getTimestamp() * 1000L) - maxCacheAgeInMilis
            ));

            String[] parts = statement.toString().split(" ");

            return String.join(" ", Arrays.copyOfRange(
                parts, 1, parts.length
            ));
        }
    }
}
