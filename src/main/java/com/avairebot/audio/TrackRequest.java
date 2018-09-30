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

package com.avairebot.audio;

import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.async.Future;
import com.avairebot.exceptions.NoMatchFoundException;
import com.avairebot.exceptions.TrackLoadFailedException;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.function.Consumer;

public class TrackRequest extends Future {

    private final GuildMusicManager musicManager;
    private final CommandMessage context;
    private final String trackUrl;

    TrackRequest(GuildMusicManager musicManager, CommandMessage context, String trackUrl) {
        this.musicManager = musicManager;
        this.context = context;
        this.trackUrl = trackUrl;

        musicManager.setLastActiveMessage(context);
    }

    @Override
    public void handle(final Consumer success, final Consumer<Throwable> failure) {
        handle(success, failure, null);
    }

    public void handle(final Consumer success, final Consumer<Throwable> failure, final Consumer<AudioSession> sessionConsumer) {
        Metrics.searchRequests.inc();

        AudioHandler.getDefaultAudioHandler().getPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (track == null) {
                    noMatches();
                    return;
                }

                Metrics.tracksLoaded.inc();

                success.accept(new TrackResponse(musicManager, track, trackUrl));

                AudioHandler.getDefaultAudioHandler().play(context, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.getTracks().isEmpty()) {
                    noMatches();
                    return;
                }

                Metrics.tracksLoaded.inc(playlist.getTracks().size());

                if (trackUrl.startsWith("ytsearch:") || trackUrl.startsWith("scsearch:")) {
                    if (sessionConsumer == null) {
                        trackLoaded(playlist.getTracks().get(0));
                        return;
                    }

                    sessionConsumer.accept(AudioHandler.getDefaultAudioHandler().createAudioSession(context, playlist));
                    return;
                }

                if (playlist.getTracks().isEmpty()) {
                    noMatches();
                    return;
                }

                success.accept(new TrackResponse(musicManager, playlist, trackUrl));
                AudioHandler.getDefaultAudioHandler().play(context, musicManager, playlist);
            }

            @Override
            public void noMatches() {
                Metrics.trackLoadsFailed.inc();

                failure.accept(new NoMatchFoundException(
                    context.i18nRaw("music.internal.noMatchFound", trackUrl),
                    trackUrl
                ));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Metrics.trackLoadsFailed.inc();

                failure.accept(new TrackLoadFailedException(
                    context.i18nRaw("music.internal.trackLoadFailed", exception.getMessage()),
                    exception.getMessage(),
                    exception
                ));
            }
        });
    }
}
