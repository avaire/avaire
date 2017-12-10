package com.avairebot.orion.commands.music;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.VoiceConnectStatus;
import com.avairebot.orion.chat.SimplePaginator;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.help.HelpCommand;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PlaylistCommand extends Command {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    public PlaylistCommand(Orion orion) {
        super(orion);
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
    public String getExampleUsage() {
        return String.join("\n",
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
            "has-role:DJ",
            "throttle:user,2,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Collection playlists = getGuildAndPlaylists(message);
        if (playlists == null) {
            return sendErrorMessage(message, "An error occurred while loading the servers playlists, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        GuildTransformer transformer = GuildController.fetchGuild(orion, message);
        if (transformer == null) {
            return sendErrorMessage(message, "An error occurred while loading the server settings, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        if (args.length == 0 && playlists.isEmpty()) {
            return sendNoPlaylistsForGuildMessage(message);
        }

        if (args.length == 0 || (args.length == 1 && NumberUtil.parseInt(args[0], -1, 10) > 0)) {
            return sendPlaylists(message, args, transformer, playlists);
        }

        List<DataRow> playlistItems = playlists.whereLoose("name", args[0]);
        if (playlistItems.isEmpty()) {
            if (args.length > 1 && (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("c"))) {
                return createPlaylist(message, args, transformer, playlists);
            }

            MessageFactory.makeWarning(message, "There are no playlist called `:playlist`, you can create the playlist by using the\n`:command` command")
                .set("command", generateCommandTrigger(message) + " <name> create")
                .set("playlist", args[0])
                .queue();

            return false;
        }

        if (playlistItems.size() == 1 && (args.length == 1 || (args.length == 2 && NumberUtil.parseInt(args[1], -1, 10) > 0))) {
            return sendSongsInPlaylist(message, args, new PlaylistTransformer(playlistItems.get(0)));
        }

        PlaylistTransformer playlist = new PlaylistTransformer(playlistItems.get(0));

        switch (args[1].toLowerCase()) {
            case "l":
            case "load":
            case "play":
                return loadPlaylist(message, args, playlist);

            case "a":
            case "add":
                return addSongToPlaylist(message, args, transformer, playlist);

            case "remove":
            case "removesong":
                return removeSongFromPlaylist(message, args, playlist);

            case "rename":
            case "renameto":
                return renamePlaylist(message, args, playlist, playlists);

            case "delete":
                return deletePlaylist(message, args, playlist);

            case "c":
            case "create":
                return createPlaylist(message, args, transformer, playlists);
        }

        return sendErrorMessage(message, String.format(
            "Invalid `property` given, there are no playlist properties called `%s`.\nYou can learn more by running `%shelp %s`",
            args[1],
            CommandHandler.getCommand(HelpCommand.class).getCategory().getPrefix(message),
            getTriggers().get(0)
        ));
    }

    private boolean loadPlaylist(Message message, String[] args, PlaylistTransformer playlist) {
        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(message);
        if (!voiceConnectStatus.isSuccess()) {
            MessageFactory.makeWarning(message, voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        AudioHandler.getGuildAudioPlayer(message.getGuild()).setLastActiveMessage(message);

        for (PlaylistTransformer.PlaylistSong song : playlist.getSongs()) {
            AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(AudioHandler.MUSIC_MANAGER, song.getLink(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    AudioHandler.getGuildAudioPlayer(message.getGuild())
                        .getScheduler().queue(track, message.getAuthor());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    trackLoaded(playlist.getTracks().get(0));
                }

                @Override
                public void noMatches() {
                    //
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    //
                }
            });
        }

        return true;
    }

    private boolean addSongToPlaylist(Message message, String[] args, GuildTransformer transformer, PlaylistTransformer playlist) {
        String query = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (query.trim().length() == 0) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `song` property!\n`:command`")
                .set("command", generateCommandTrigger(message) + " " + playlist.getName() + " add <song title / link>")
                .queue();

            return false;
        }

        if (playlist.getSongs().size() >= transformer.getType().getLimits().getPlaylist().getSongs()) {
            MessageFactory.makeWarning(message, "The `:playlist` playlist doesn't have any more song slots.")
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        try {
            new URL(query);
        } catch (MalformedURLException ex) {
            query = "ytsearch:" + query;
        }

        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(AudioHandler.MUSIC_MANAGER, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (track.getInfo().isStream) {
                    MessageFactory.makeWarning(message, "You can't add livestreams to a playlist!").queue();
                    return;
                }

                playlist.addSong(
                    track.getInfo().title,
                    NumberUtil.formatTime(track.getDuration()),
                    track.getInfo().uri
                );

                try {
                    orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                        .where("id", playlist.getId()).andWhere("guild_id", message.getGuild().getId())
                        .update(statement -> {
                            statement.set("songs", GSON.toJson(playlist.getSongs()));
                            statement.set("size", playlist.getSongs().size());
                        });

                    MessageFactory.makeSuccess(message, ":user has added [:name](:url) to the `:playlist` playlist.\nThe `:playlist` playlist has `:slots` more song slots available.")
                        .set("name", track.getInfo().title)
                        .set("url", track.getInfo().uri)
                        .set("playlist", playlist.getName())
                        .set("slots", transformer.getType().getLimits().getPlaylist().getSongs() - playlist.getSongs().size())
                        .queue();
                } catch (SQLException e) {
                    e.printStackTrace();

                    MessageFactory.makeError(message, "Something went wrong while trying to save the playlist: " + e.getMessage()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                MessageFactory.makeWarning(message, "No Matches").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                MessageFactory.makeError(message, "Failed to load: " + e.getMessage()).queue();
            }
        });
        return true;
    }

    private boolean removeSongFromPlaylist(Message message, String[] args, PlaylistTransformer playlist) {
        if (args.length < 3) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `song id` property!\n`:command`")
                .set("command", generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                .queue();

            return false;
        }

        if (playlist.getSongs().isEmpty()) {
            MessageFactory.makeWarning(message, "The `:playlist` playlist is already empty, there is nothing to remove.")
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        try {
            int id = Integer.parseInt(args[2], 10) - 1;
            if (id < 0 || id >= playlist.getSongs().size()) {
                MessageFactory.makeWarning(message, "Invalid id given, the number given is too :type\n`:command`")
                    .set("command", generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                    .set("type", id < 0 ? "low" : "high")
                    .queue();

                return false;
            }

            PlaylistTransformer.PlaylistSong removed = playlist.getSongs().remove(id);

            orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", GSON.toJson(playlist.getSongs()));
                    statement.set("size", playlist.getSongs().size());
                });

            MessageFactory.makeSuccess(message, ":song has been successfully removed from the `:playlist` playlist")
                .set("song", String.format("[%s](%s)", removed.getTitle(), removed.getLink()))
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (NumberFormatException e) {
            MessageFactory.makeWarning(message, "Invalid id given, the id must be a number\n`:command`")
                .set("command", generateCommandTrigger(message) + " " + playlist.getName() + " removesong <song id>")
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean renamePlaylist(Message message, String[] args, PlaylistTransformer playlist, Collection playlists) {
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (name.trim().length() == 0) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `new name` property!\n`:command`")
                .set("command", generateCommandTrigger(message) + " " + playlist.getName() + " renameto <new name>")
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
                    statement.set("name", playlist.getName());
                });

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

    private boolean deletePlaylist(Message message, String[] args, PlaylistTransformer playlist) {
        try {
            orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("guild_id", message.getGuild().getId())
                .andWhere("id", playlist.getId())
                .delete();

            MessageFactory.makeSuccess(message, "The `:name` playlist has been deleted successfully!")
                .set("name", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            MessageFactory.makeError(message, "Error: " + e.getMessage()).queue();
        }

        return false;
    }

    private boolean createPlaylist(Message message, String[] args, GuildTransformer transformer, Collection playlists) {
        String name = args[0];
        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            MessageFactory.makeWarning(message, "The `:playlist` playlist already exists!")
                .set("playlist", name).queue();
            return false;
        }

        int playlistLimit = transformer.getType().getLimits().getPlaylist().getPlaylists();
        if (playlists.size() >= playlistLimit) {
            MessageFactory.makeWarning(message, "The server doesn't have any more playlist slots, you can delete existing playlists to free up slots.").queue();
            return false;
        }

        try {
            orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .insert(statement -> {
                    statement.set("guild_id", message.getGuild().getId());
                    statement.set("name", name);
                    statement.set("size", 0);
                    statement.set("songs", GSON.toJson(new ArrayList<>()));
                });

            MessageFactory.makeSuccess(message,
                "The `:playlist` playlist has been been created successfully!\nYou can start adding songs to it with `:command :playlist add <song>`"
            ).set("playlist", name).set("command", generateCommandTrigger(message)).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            MessageFactory.makeError(message, "Error: " + e.getMessage()).queue();
        }

        return false;
    }

    private boolean sendPlaylists(Message message, String[] args, GuildTransformer transformer, Collection playlists) {
        SimplePaginator paginator = new SimplePaginator(playlists.sort(
            Comparator.comparing(dataRow -> dataRow.getString("name"))
        ).getItems(), 5);

        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            messages.add(String.format("%s\n   ‍   Playlist has **%s** song(s)",
                row.getString("name"), row.getInt("size")
            ));
        });

        String counter = String.format(" [ %s out of %s ]",
            playlists.size(), transformer.getType().getLimits().getPlaylist().getPlaylists()
        );

        MessageFactory.makeInfo(message, "\u2022 " +
            String.join("\n\u2022 ", messages) + "\n\n" +
            paginator.generateFooter(generateCommandTrigger(message))
        ).setTitle(":musical_note: Music Playlist " + counter).queue();

        return true;
    }

    private boolean sendSongsInPlaylist(Message message, String[] args, PlaylistTransformer playlist) {
        if (playlist.getSongs().isEmpty()) {
            MessageFactory.makeWarning(message, "There are no songs in this playlist, you can add songs to it by using the\n`:command` command.")
                .set("command", generateCommandTrigger(message) + " add <song url>")
                .queue();

            return false;
        }

        SimplePaginator paginator = new SimplePaginator(playlist.getSongs(), 10);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            PlaylistTransformer.PlaylistSong song = (PlaylistTransformer.PlaylistSong) val;

            messages.add(String.format("`%s` [%s](%s) [%s]",
                index + 1,
                song.getTitle(),
                song.getLink(),
                song.getDuration()
            ));
        });

        MessageFactory.makeInfo(message,
            String.join("\n", messages) + "\n\n" + paginator.generateFooter(generateCommandTrigger(message))
        ).setTitle(":musical_note: " + playlist.getName()).queue();

        return true;
    }

    private boolean sendNoPlaylistsForGuildMessage(Message message) {
        MessageFactory.makeInfo(message, "This server does not have any music playlists yet, you can create one with\n`:command` to get started")
            .set("command", generateCommandTrigger(message) + " <name> create")
            .setTitle(":musical_note: Music Playlists")
            .queue();

        return false;
    }

    private Collection getGuildAndPlaylists(Message message) {
        try {
            return orion.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .selectAll().where("guild_id", message.getGuild().getId())
                .get();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
