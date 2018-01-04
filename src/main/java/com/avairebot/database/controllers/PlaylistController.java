package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.PlaylistTransformer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.CheckReturnValue;
import java.util.List;

public class PlaylistController {

    private static final String CACHE_STRING = "playlists.";

    @CheckReturnValue
    public static Collection fetchPlaylists(AvaIre avaire, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).remember(getCacheString(message.getGuild()), 300, () -> {
            return avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .selectAll().where("guild_id", message.getGuild().getId())
                .get();
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

    public static String getCacheString(Guild guild) {
        return CACHE_STRING + guild.getId();
    }
}
