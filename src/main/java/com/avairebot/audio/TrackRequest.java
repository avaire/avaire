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

import com.avairebot.audio.exceptions.InvalidSearchProviderException;
import com.avairebot.audio.exceptions.SearchingException;
import com.avairebot.audio.exceptions.TrackLoadFailedException;
import com.avairebot.audio.searcher.SearchProvider;
import com.avairebot.audio.searcher.SearchTrackResultHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.async.Future;
import com.avairebot.exceptions.NoMatchFoundException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

import java.util.function.Consumer;

public class TrackRequest extends Future {

    private final GuildMusicManager musicManager;
    private final CommandMessage context;
    private final TrackRequestContext trackContext;

    TrackRequest(GuildMusicManager musicManager, CommandMessage context, TrackRequestContext trackContext) {
        this.musicManager = musicManager;
        this.context = context;
        this.trackContext = trackContext;

        musicManager.setLastActiveMessage(context);
    }

    @Override
    public void handle(final Consumer success, final Consumer<Throwable> failure) {
        handle(success, failure, null);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public void handle(final Consumer success, final Consumer<Throwable> failure, final Consumer<AudioSession> sessionConsumer) {
        try {
            AudioPlaylist playlist = new SearchTrackResultHandler(trackContext).searchSync();

            if (playlist.getTracks() == null || playlist.getTracks().isEmpty()) {
                failure.accept(new NoMatchFoundException(
                    context.i18nRaw("music.internal.noMatchFound", trackContext.getQuery()),
                    trackContext
                ));
            } else if (sessionConsumer != null && isSearchableContext(trackContext)) {
                sessionConsumer.accept(AudioHandler.getDefaultAudioHandler().createAudioSession(context, playlist));
            } else {
                success.accept(new TrackResponse(musicManager, playlist, trackContext));

                if (playlist.getTracks().size() > 1) {
                    AudioHandler.getDefaultAudioHandler().play(
                        context, musicManager, playlist
                    );
                } else {
                    AudioHandler.getDefaultAudioHandler().play(
                        context, musicManager, playlist.getTracks().get(0)
                    );
                }
            }
        } catch (InvalidSearchProviderException | TrackLoadFailedException e) {
            failure.accept(new FriendlyException(
                context.i18nRaw("music.internal.trackLoadFailed", e.getMessage()),
                FriendlyException.Severity.COMMON,
                e
            ));
        } catch (SearchingException e) {
            failure.accept(new NoMatchFoundException(
                context.i18nRaw("music.internal.noMatchFound", trackContext.getQuery()),
                trackContext
            ));
        }
    }

    private boolean isSearchableContext(TrackRequestContext trackContext) {
        return trackContext.getProvider().equals(SearchProvider.YOUTUBE)
            || trackContext.getProvider().equals(SearchProvider.SOUNDCLOUD);
    }
}
