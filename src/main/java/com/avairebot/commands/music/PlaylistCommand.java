package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.help.HelpCommand;
import com.avairebot.commands.music.playlist.*;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

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
            "has-dj-level:normal",
            "throttle:user,2,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Collection playlists = PlaylistController.fetchPlaylists(avaire, message);
        if (playlists == null) {
            return sendErrorMessage(message, "An error occurred while loading the servers playlists, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);
        if (transformer == null) {
            return sendErrorMessage(message, "An error occurred while loading the server settings, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        if (args.length == 0 && playlists.isEmpty()) {
            return sendNoPlaylistsForGuildMessage(message);
        }

        if (args.length == 0 || (args.length == 1 && NumberUtil.isNumeric(args[0]))) {
            return sendPlaylists.onCommand(message, args, transformer, playlists);
        }

        List<DataRow> playlistItems = playlists.whereLoose("name", args[0]);
        if (playlistItems.isEmpty()) {
            if (args.length > 1 && (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("c"))) {
                return createPlaylist.onCommand(message, args, transformer, playlists);
            }

            MessageFactory.makeWarning(message, "There are no playlist called `:playlist`, you can create the playlist by using the\n`:command` command")
                .set("command", generateCommandTrigger(message) + " <name> create")
                .set("playlist", args[0])
                .queue();

            return false;
        }

        if (playlistItems.size() == 1 && (args.length == 1 || (args.length == 2 && NumberUtil.isNumeric(args[1])))) {
            return sendSongsInPlaylist.onCommand(message, args, transformer, new PlaylistTransformer(playlistItems.get(0)));
        }

        PlaylistTransformer playlist = new PlaylistTransformer(playlistItems.get(0));

        switch (args[1].toLowerCase()) {
            case "l":
            case "load":
            case "play":
                return loadPlaylist.onCommand(message, args, transformer, playlist);

            case "a":
            case "add":
                return addSongToPlaylist.onCommand(message, args, transformer, playlist);

            case "remove":
            case "removesong":
                return removeSongFromLoadPlaylist.onCommand(message, args, transformer, playlist);

            case "rename":
            case "renameto":
                return renamePlaylist.onCommand(message, args, transformer, playlist, playlists);

            case "delete":
                return deletePlaylist.onCommand(message, args, transformer, playlist);

            case "c":
            case "create":
                return createPlaylist.onCommand(message, args, transformer, playlists);
        }

        return sendErrorMessage(message, String.format(
            "Invalid `property` given, there are no playlist properties called `%s`.\nYou can learn more by running `%shelp %s`",
            args[1],
            CommandHandler.getCommand(HelpCommand.class).getCategory().getPrefix(message),
            getTriggers().get(0)
        ));
    }

    private boolean sendNoPlaylistsForGuildMessage(Message message) {
        MessageFactory.makeInfo(message, "This server does not have any music playlists yet, you can create one with\n`:command` to get started")
            .set("command", generateCommandTrigger(message) + " <name> create")
            .setTitle(":musical_note: Music Playlists")
            .queue();

        return false;
    }
}
