package com.avairebot.orion.audio;

import com.avairebot.orion.contracts.async.Future;
import com.avairebot.orion.exceptions.NoMatchFoundException;
import com.avairebot.orion.exceptions.TrackLoadFailedException;
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

    public TrackRequest(GuildMusicManager musicManager, Message message, String trackUrl) {
        this.musicManager = musicManager;
        this.message = message;
        this.trackUrl = trackUrl;

        musicManager.setLastActiveMessage(message);
    }

    @Override
    public void handle(Consumer success, Consumer<Throwable> failure) {
        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                success.accept(new TrackResponse(musicManager, track, trackUrl));

                AudioHandler.play(message, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                success.accept(new TrackResponse(musicManager, playlist, trackUrl));

                for (AudioTrack track : playlist.getTracks()) {
                    AudioHandler.play(message, musicManager, track);
                }
            }

            @Override
            public void noMatches() {
                failure.accept(new NoMatchFoundException("I found nothing with the given query: `%s`", trackUrl));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                failure.accept(new TrackLoadFailedException("I couldn't add that to the queue: `%s`", exception.getMessage(), exception));
            }
        });
    }
}
