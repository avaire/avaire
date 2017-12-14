package com.avairebot.orion.database.controllers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.CheckReturnValue;
import java.util.List;

public class PlaylistController {

    private static final String CACHE_STRING = "playlists.";

    @CheckReturnValue
    public static Collection fetchPlaylists(Orion orion, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return (Collection) orion.getCache().getAdapter(CacheType.MEMORY).remember(getCacheString(message.getGuild()), 300, () -> {
            return orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .selectAll().where("guild_id", message.getGuild().getId())
                .get();
        });
    }

    @CheckReturnValue
    public static PlaylistTransformer fetchPlaylistFromName(Orion orion, Message message, String name) {
        Collection playlists = fetchPlaylists(orion, message);
        if (playlists == null) {
            return null;
        }

        List<DataRow> playlist = playlists.whereLoose("name", name);
        if (playlist.isEmpty()) {
            return null;
        }

        return new PlaylistTransformer(playlist.get(0));
    }

    public static String getCacheString(Guild guild) {
        return CACHE_STRING + guild.getId();
    }
}
