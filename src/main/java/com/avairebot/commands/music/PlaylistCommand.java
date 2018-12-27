/*
 * Copyright (c) 2018.
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

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.help.HelpCommand;
import com.avairebot.commands.music.playlist.*;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.connections.SQLite;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaylistCommand extends Command {

    private final RemoveSongFromPlaylist removeSongFromLoadPlaylist;
    private final SendSongsInPlaylist sendSongsInPlaylist;
    private final MoveSongInPlaylist moveSongInPlaylist;
    private final AddSongToPlaylist addSongToPlaylist;
    private final CreatePlaylist createPlaylist;
    private final DeletePlaylist deletePlaylist;
    private final RenamePlaylist renamePlaylist;
    private final SendPlaylists sendPlaylists;
    private final LoadPlaylist loadPlaylist;

    public PlaylistCommand(AvaIre avaire) {
        super(avaire, false);

        removeSongFromLoadPlaylist = new RemoveSongFromPlaylist(avaire, this);
        sendSongsInPlaylist = new SendSongsInPlaylist(avaire, this);
        moveSongInPlaylist = new MoveSongInPlaylist(avaire, this);
        addSongToPlaylist = new AddSongToPlaylist(avaire, this);
        createPlaylist = new CreatePlaylist(avaire, this);
        deletePlaylist = new DeletePlaylist(avaire, this);
        renamePlaylist = new RenamePlaylist(avaire, this);
        sendPlaylists = new SendPlaylists(avaire, this);
        loadPlaylist = new LoadPlaylist(avaire, this);
    }

    @Override
    public String getName() {
        return "Playlist Command";
    }

    @Override
    public String getDescription() {
        return "Music playlist command, allows music DJs to create, delete, and load playlists to the music queue, as well as adding and removing songs from any of the playlists.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Lists existing playlists.",
            "`:command [name] add [song link]` - Adds a song to a playlist.",
            "`:command [name] create` - Creates a new playlist.",
            "`:command [name] delete` - Deletes an existing playlist.",
            "`:command [name] play` - Plays a playlist.",
            "`:command [name] removesong [id]` - Removes a song from a playlist.",
            "`:command [name] renameto [new name]` - Renames a existing playlist.",
            "`:command [name] movesong [id] [new id]` - Move a song to a different position.",
            "`:command [name] [page number]` - Shows the songs in a playlist."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command test create` - Creates a playlist called `test`.",
            "`:command test add Some song` - Adds `Some song` to the `test` playlist.",
            "`:command test move 2 1` - Moves the 2nd song to the first place.",
            "`:command test remove 2` - Removes the 2nd song from the `test`playlist.",
            "`:command test rename Music` - Renames the `test` playlist to `Music`.",
            "`:command music 2` - Shows the 2nd page of the `Music` playlist.",
            "`:command music play` - Plays all the songs in the `Music` playlist.",
            "`:command music delete` - Deletes the `Music` playlist."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("playlist", "list", "pl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:user,2,5",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Arrays.asList(
            CommandGroups.MUSIC_START_PLAYING,
            CommandGroups.MISCELLANEOUS
        );
    }

    @Override
    @SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
    public boolean onCommand(CommandMessage context, String[] args) {
        if (isRunningOutdatedSQLite()) {
            return sendErrorMessage(context, "The current selected database is running an outdated version of the `SQLite` setup, if you want to use the playlist commands, either re-generate the SQLite database to get the new version, or change to a `MySQL` setup instead.");
        }

        Collection playlists = PlaylistController.fetchPlaylists(avaire, context.getMessage());
        if (playlists == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "servers playlist");
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (args.length == 0 && playlists.isEmpty()) {
            return sendNoPlaylistsForGuildMessage(context);
        }

        if (args.length == 0 || (args.length == 1 && NumberUtil.isNumeric(args[0]))) {
            return sendPlaylists.onCommand(context, args, transformer, playlists);
        }

        List<DataRow> playlistItems = playlists.whereLoose("name", args[0]);
        if (playlistItems.isEmpty()) {
            if (args.length > 1 && (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("c"))) {
                return createPlaylist.onCommand(context, args, transformer, playlists);
            }

            context.makeWarning(context.i18n("noPlaylistWithName"))
                .set("command", generateCommandTrigger(context.getMessage()) + " <name> create")
                .set("playlist", args[0])
                .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

            return false;
        }

        if (playlistItems.size() == 1 && (args.length == 1 || (args.length == 2 && NumberUtil.isNumeric(args[1])))) {
            return sendSongsInPlaylist.onCommand(context, args, transformer, new PlaylistTransformer(playlistItems.get(0)));
        }

        PlaylistTransformer playlist = new PlaylistTransformer(playlistItems.get(0));

        switch (args[1].toLowerCase()) {
            case "l":
            case "load":
            case "play":
                return loadPlaylist.onCommand(context, args, transformer, playlist);

            case "a":
            case "add":
                return addSongToPlaylist.onCommand(context, args, transformer, playlist);

            case "swap":
            case "move":
            case "movesong":
                return moveSongInPlaylist.onCommand(context, args, transformer, playlist);

            case "remove":
            case "removesong":
                return removeSongFromLoadPlaylist.onCommand(context, args, transformer, playlist);

            case "rename":
            case "renameto":
                return renamePlaylist.onCommand(context, args, transformer, playlist, playlists);

            case "delete":
                return deletePlaylist.onCommand(context, args, transformer, playlist);

            case "c":
            case "create":
                return createPlaylist.onCommand(context, args, transformer, playlists);
        }

        return sendErrorMessage(context, context.i18n("invalidProperty",
            args[1],
            CommandHandler.getCommand(HelpCommand.class).getCategory().getPrefix(context.getMessage()),
            getTriggers().get(0)
        ));
    }

    private boolean sendNoPlaylistsForGuildMessage(CommandMessage context) {
        context.makeInfo(context.i18n("noPlaylists"))
            .set("command", generateCommandTrigger(context.getMessage()) + " <name> create")
            .setTitle(":musical_note: Music Playlists")
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return false;
    }

    private boolean isRunningOutdatedSQLite() {
        try {
            if (avaire.getDatabase().getConnection() instanceof SQLite && !avaire.getDatabase().getSchema().hasColumn(
                Constants.MUSIC_PLAYLIST_TABLE_NAME, "amount"
            )) {
                return true;
            }
        } catch (SQLException ignored) {
        }
        return false;
    }
}
