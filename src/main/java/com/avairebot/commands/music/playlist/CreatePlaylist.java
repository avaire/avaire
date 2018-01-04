package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreatePlaylist extends PlaylistSubCommand {

    public CreatePlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, Collection playlists) {
        String name = args[0].trim().split(" ")[0];
        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            MessageFactory.makeWarning(message, "The `:playlist` playlist already exists!")
                .set("playlist", name).queue();
            return false;
        }

        if (NumberUtil.isNumeric(name)) {
            MessageFactory.makeWarning(message, "The playlist can't only be numbers, you have to include some letters in the name!")
                .queue();
            return false;
        }

        int playlistLimit = guild.getType().getLimits().getPlaylist().getPlaylists();
        if (playlists.size() >= playlistLimit) {
            MessageFactory.makeWarning(message, "The server doesn't have any more playlist slots, you can delete existing playlists to free up slots.").queue();
            return false;
        }

        try {
            storeInDatabase(message, name);

            MessageFactory.makeSuccess(message,
                "The `:playlist` playlist has been been created successfully!\nYou can start adding songs to it with `:command :playlist add <song>`"
            ).set("playlist", name).set("command", command.generateCommandTrigger(message)).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            MessageFactory.makeError(message, "Error: " + e.getMessage()).queue();
        }

        return false;
    }

    private void storeInDatabase(Message message, String name) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
            .insert(statement -> {
                statement.set("guild_id", message.getGuild().getId());
                statement.set("name", name, true);
                statement.set("amount", 0);
                statement.set("songs", AvaIre.GSON.toJson(new ArrayList<>()));
            });

        avaire.getCache().getAdapter(CacheType.MEMORY)
            .forget(PlaylistController.getCacheString(message.getGuild()));
    }
}
