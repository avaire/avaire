package com.avairebot.audio;

import com.avairebot.contracts.async.Future;
import com.avairebot.exceptions.NoMatchFoundException;
import com.avairebot.exceptions.TrackLoadFailedException;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public class TrackRequest extends Future {

    private final GuildMusicManager musicManager;
    private final Message message;
    private final String trackUrl;

    TrackRequest(GuildMusicManager musicManager, Message message, String trackUrl) {
        this.musicManager = musicManager;
        this.message = message;
        this.trackUrl = trackUrl;

        musicManager.setLastActiveMessage(message);
    }

    @Override
    public void handle(final Consumer success, final Consumer<Throwable> failure) {
        handle(success, failure, null);
    }

    public void handle(final Consumer success, final Consumer<Throwable> failure, final Consumer<AudioSession> sessionConsumer) {
        Metrics.searchRequests.inc();

        AudioHandler.getPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Metrics.tracksLoaded.inc();

                success.accept(new TrackResponse(musicManager, track, trackUrl));

                AudioHandler.play(message, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (trackUrl.startsWith("ytsearch:") || trackUrl.startsWith("scsearch:")) {
                    if (sessionConsumer == null) {
                        trackLoaded(playlist.getTracks().get(0));
                        return;
                    }

                    sessionConsumer.accept(AudioHandler.createAudioSession(message, playlist));
                    return;
                }

                success.accept(new TrackResponse(musicManager, playlist, trackUrl));

                Metrics.tracksLoaded.inc(playlist.getTracks().size());
                AudioHandler.play(message, musicManager, playlist);
            }

            @Override
            public void noMatches() {
                Metrics.trackLoadsFailed.inc();
                failure.accept(new NoMatchFoundException("I found nothing with the given query: `%s`", trackUrl));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Metrics.trackLoadsFailed.inc();
                failure.accept(new TrackLoadFailedException("I couldn't add that to the queue: `%s`", exception.getMessage(), exception));
            }
        });
    }
}
