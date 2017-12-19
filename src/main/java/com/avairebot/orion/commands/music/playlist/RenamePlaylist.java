package com.avairebot.orion.commands.music.playlist;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.controllers.PlaylistController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RenamePlaylist extends PlaylistSubCommand {

    public RenamePlaylist(Orion orion, PlaylistCommand command) {
        super(orion, command);
    }

    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist, Collection playlists) {
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (name.trim().length() == 0) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `new name` property!\n`:command`")
                .set("command", command.generateCommandTrigger(message) + " " + playlist.getName() + " renameto <new name>")
                .queue();

            return false;
        }

        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            MessageFactory.makeInfo(message, "Can\'t rename the `:oldplaylist` to `:playlist`, there are already a playlist called `:playlist`")
                .set("oldplaylist", playlist.getName())
                .set("playlist", name)
                .queue();

            return false;
        }

        String oldPlaylist = playlist.getName();
        playlist.setName(name);

        try {
            orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("name", playlist.getName(), true);
                });

            orion.getCache().getAdapter(CacheType.MEMORY)
                .forget(PlaylistController.getCacheString(message.getGuild()));

            MessageFactory.makeSuccess(message, "The `:oldplaylist` playlist has been renamed to `:playlist`!")
                .set("oldplaylist", oldPlaylist)
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            MessageFactory.makeError(message, "Something went wrong while trying to save the playlist: " + e.getMessage()).queue();
        }

        return false;
    }
}
