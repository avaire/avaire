package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RenamePlaylist extends PlaylistSubCommand {

    public RenamePlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist, Collection playlists) {
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (name.trim().length() == 0) {
            context.makeWarning("Invalid format, missing the `new name` property!\n`:command`")
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " renameto <new name>")
                .queue();

            return false;
        }

        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            context.makeInfo("Can\'t rename the `:oldplaylist` to `:playlist`, there are already a playlist called `:playlist`")
                .set("oldplaylist", playlist.getName())
                .set("playlist", name)
                .queue();

            return false;
        }

        String oldPlaylist = playlist.getName();
        playlist.setName(name);

        try {
            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("name", playlist.getName(), true);
                });

            avaire.getCache().getAdapter(CacheType.MEMORY)
                .forget(PlaylistController.getCacheString(context.getGuild()));

            context.makeSuccess("The `:oldplaylist` playlist has been renamed to `:playlist`!")
                .set("oldplaylist", oldPlaylist)
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            context.makeError("Something went wrong while trying to save the playlist: " + e.getMessage()).queue();
        }

        return false;
    }
}
