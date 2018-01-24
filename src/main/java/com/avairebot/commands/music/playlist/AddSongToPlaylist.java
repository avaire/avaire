package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.AudioHandler;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
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

    public AddSongToPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
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
        Metrics.searchRequests.inc();

        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(AudioHandler.MUSIC_MANAGER, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Metrics.tracksLoaded.inc();

                handleTrackLoadedEvent(message, guild, playlist, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                Metrics.trackLoadsFailed.inc();
                MessageFactory.makeWarning(message, "No Matches").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                Metrics.trackLoadsFailed.inc();
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
            avaire.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", AvaIre.GSON.toJson(playlist.getSongs()), true);
                    statement.set("amount", playlist.getSongs().size());
                });

            avaire.getCache().getAdapter(CacheType.MEMORY)
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
