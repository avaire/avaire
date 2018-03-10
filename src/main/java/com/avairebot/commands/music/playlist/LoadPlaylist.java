package com.avairebot.commands.music.playlist;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.VoiceConnectStatus;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlaylistCommand;
import com.avairebot.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoadPlaylist extends PlaylistSubCommand {

    public LoadPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(context);
        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        List<AudioTrack> tracks = new ArrayList<>();
        AudioHandler.getGuildAudioPlayer(context.getGuild()).setLastActiveMessage(context);

        int index = 0;
        for (PlaylistTransformer.PlaylistSong song : playlist.getSongs()) {
            if (index++ == playlist.getSongs().size() - 1) {
                loadSong(song, tracks, audioTracks -> {
                    loadedPlaylist(context, playlist, tracks);
                });

                return true;
            }
            loadSong(song, tracks, null);
        }

        context.makeWarning(context.i18n("failedToLoadPlaylist"))
            .set("playlist", playlist.getName())
            .queue();

        return true;
    }

    private void loadedPlaylist(CommandMessage context, PlaylistTransformer playlist, List<AudioTrack> tracks) {
        if (tracks.isEmpty()) {
            context.makeWarning(context.i18n("failedToLoadPlaylist"))
                .set("playlist", playlist.getName())
                .queue();

            return;
        }

        // We assign the size here because the track might be
        // modified if nothing is playing by queue method.
        int size = tracks.size();

        AudioHandler.getGuildAudioPlayer(context.getGuild())
            .getScheduler().queue(tracks, context.getAuthor());

        context.makeSuccess(context.i18n("loadedPlaylist"))
            .set("name", playlist.getName())
            .set("amount", size)
            .queue();
    }

    private void loadSong(PlaylistTransformer.PlaylistSong song, final List<AudioTrack> tracks, Consumer<List<AudioTrack>> success) {
        Metrics.searchRequests.inc();

        AudioHandler.getPlayerManager().loadItemOrdered(AudioHandler.MUSIC_MANAGER, song.getLink(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Metrics.tracksLoaded.inc();

                tracks.add(track);

                if (success != null) {
                    success.accept(tracks);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                Metrics.trackLoadsFailed.inc();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Metrics.trackLoadsFailed.inc();
            }
        });
    }
}
