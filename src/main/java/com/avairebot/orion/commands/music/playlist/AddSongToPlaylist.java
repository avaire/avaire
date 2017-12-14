package com.avairebot.orion.commands.music.playlist;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.orion.database.controllers.PlaylistController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;

public class AddSongToPlaylist extends PlaylistSubCommand {

    public AddSongToPlaylist(Orion orion, PlaylistCommand command) {
        super(orion, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        String query = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (query.trim().length() == 0) {
            MessageFactory.makeWarning(message, "Invalid format, missing the `song` property!\n`:command`")
                .set("command", command.generateCommandTrigger(message) + " " + playlist.getName() + " add <song title / link>")
                .queue();

            return false;
        }

        if (playlist.getSongs().size() >= guild.getType().getLimits().getPlaylist().getSongs()) {
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

        String finalQuery = query;
        message.getChannel().sendTyping().queue(v -> loadSong(message, finalQuery, guild, playlist));
        return true;
    }

    private void loadSong(Message message, String query, GuildTransformer guild, PlaylistTransformer playlist) {
        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(AudioHandler.MUSIC_MANAGER, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                handleTrackLoadedEvent(message, guild, playlist, track);
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
    }

    private void handleTrackLoadedEvent(Message message, GuildTransformer guild, PlaylistTransformer playlist, AudioTrack track) {
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

            orion.getCache().getAdapter(CacheType.MEMORY)
                .forget(PlaylistController.getCacheString(message.getGuild()));

            MessageFactory.makeSuccess(message, ":user has added [:name](:url) to the `:playlist` playlist.\nThe `:playlist` playlist has `:slots` more song slots available.")
                .set("name", track.getInfo().title)
                .set("url", track.getInfo().uri)
                .set("playlist", playlist.getName())
                .set("slots", guild.getType().getLimits().getPlaylist().getSongs() - playlist.getSongs().size())
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();

            MessageFactory.makeError(message, "Something went wrong while trying to save the playlist: " + e.getMessage()).queue();
        }
    }
}
