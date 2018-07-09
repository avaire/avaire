package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaylistController {

    public static final Cache<Object, Object> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    @CheckReturnValue
    public static Collection fetchPlaylists(AvaIre avaire, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, message.getGuild().getIdLong(), () -> {
            try {
                return avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                    .selectAll().where("guild_id", message.getGuild().getId())
                    .get();
            } catch (Exception e) {
                AvaIre.getLogger().error("Failed to fetch playlists for server " + message.getGuild().getId(), e);
                return null;
            }
        });
    }

    @CheckReturnValue
    public static PlaylistTransformer fetchPlaylistFromName(AvaIre avaire, Message message, String name) {
        Collection playlists = fetchPlaylists(avaire, message);
        if (playlists == null) {
            return null;
        }

        List<DataRow> playlist = playlists.whereLoose("name", name);
        if (playlist.isEmpty()) {
            return null;
        }

        return new PlaylistTransformer(playlist.get(0));
    }

    public static void forgetCache(long guildId) {
        cache.invalidate(guildId);
    }
}
