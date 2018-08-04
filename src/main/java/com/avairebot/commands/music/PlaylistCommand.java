package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.help.HelpCommand;
import com.avairebot.commands.music.playlist.*;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.connections.SQLite;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaylistCommand extends Command {

    private final RemoveSongFromPlaylist removeSongFromLoadPlaylist;
    private final SendSongsInPlaylist sendSongsInPlaylist;
    private final AddSongToPlaylist addSongToPlaylist;
    private final CreatePlaylist createPlaylist;
    private final DeletePlaylist deletePlaylist;
    private final RenamePlaylist renamePlaylist;
    private final SendPlaylists sendPlaylists;
    private final LoadPlaylist loadPlaylist;

    public PlaylistCommand(AvaIre avaire) {
        super(avaire);

        removeSongFromLoadPlaylist = new RemoveSongFromPlaylist(avaire, this);
        sendSongsInPlaylist = new SendSongsInPlaylist(avaire, this);
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
            "`:command [name] [page number]` - Shows the songs in a playlist."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command test create` - Creates a playlist called `test`.",
            "`:command test add Some song` - Adds `Some song` to the `test` playlist.",
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

    @Override
    @SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
    public boolean onCommand(CommandMessage context, String[] args) {
        try {
            if (avaire.getDatabase().getConnection() instanceof SQLite) {
                return sendErrorMessage(context, "The current selected database type is set to `SQLite`, the playlist command does not support the SQLite syntax(yet), if you want to use the playlist commands, please change to a `MySQL` setup instead.");
            }
        } catch (SQLException ignored) {
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
}
