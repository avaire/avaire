/*
 * Copyright (c) 2019.
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

package com.avairebot.audio.seracher;

import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.TrackRequestContext;
import com.avairebot.audio.exceptions.Http503Exception;
import com.avairebot.audio.exceptions.InvalidSearchProviderException;
import com.avairebot.audio.exceptions.SearchingException;
import com.avairebot.audio.exceptions.TrackLoadFailedException;
import com.avairebot.database.controllers.SearchController;
import com.avairebot.database.transformers.SearchResultTransformer;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SearchTrackResultHandler implements AudioLoadResultHandler {

    private static final Logger log = LoggerFactory.getLogger(SearchTrackResultHandler.class);
    private static final long defaultTimeout = 3000L;

    private final TrackRequestContext trackContext;
    private boolean skipCache = false;

    private Exception exception;
    private AudioPlaylist playlist;

    public SearchTrackResultHandler(TrackRequestContext trackContext) {
        this.trackContext = trackContext;
    }

    public AudioPlaylist searchSync() throws SearchingException {
        return searchSync(defaultTimeout);
    }

    public AudioPlaylist searchSync(long timeoutMillis) throws SearchingException {
        Metrics.searchRequests.inc();

        this.validateSearchProviderIsActive();

        log.debug("Searching using the {} provider for \"{}\"", trackContext.getProvider(), trackContext.getFormattedQuery());

        if (!skipCache) {
            AudioPlaylist playlist = loadContextFromCache();
            if (playlist != null) {
                log.debug("Loaded search result from the cache");
                return playlist;
            }
        }

        try {
            AudioHandler.getDefaultAudioHandler().getPlayerManager().loadItem(trackContext.getFullQueryString(), this)
                .get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            exception = e;
        } catch (TimeoutException e) {
            throw new SearchingException(String.format(
                "Searching provider %s for \"%s\" timed out after %sms",
                trackContext.getProvider().name(), trackContext.getQuery(), timeoutMillis
            ));
        }

        if (exception != null) {
            if (exception instanceof FriendlyException && exception.getCause() != null) {
                String messageOfCause = exception.getCause().getMessage();
                if (messageOfCause.contains("java.io.IOException: Invalid status code for search response: 503")) {
                    throw new Http503Exception("Lavaplayer search returned a 503", exception);
                }
            }

            throw new SearchingException(String.format(
                "Failed to search provider %s for query %s with exception %s.",
                trackContext.getProvider(), trackContext.getQuery(), exception.getMessage()
            ), exception);
        }

        if (playlist == null) {
            throw new SearchingException(String.format("Result from provider %s for query %s is unexpectedly null",
                trackContext.getProvider(), trackContext.getQuery()
            ));
        }

        if (!skipCache) {
            SearchController.cacheSearchResult(trackContext, playlist);
        }

        return playlist;
    }

    public SearchTrackResultHandler skipCache(boolean skipCache) {
        this.skipCache = skipCache;

        return this;
    }

    private void validateSearchProviderIsActive() throws InvalidSearchProviderException {
        if (!SearchProvider.YOUTUBE.isActive() && isRequestingYouTubeWithDirectLink()) {
            throw new InvalidSearchProviderException(String.format(
                "The %s search provider is disabled, you can't request music via direct links until it is re-enabled.",
                SearchProvider.YOUTUBE
            ));
        }

        if (trackContext.getProvider().isActive()) {
            return;
        }

        if (trackContext.getProvider().equals(SearchProvider.YOUTUBE) && SearchProvider.SOUNDCLOUD.isActive()) {
            trackContext.setProvider(SearchProvider.SOUNDCLOUD);
        } else if (trackContext.getProvider().equals(SearchProvider.SOUNDCLOUD) && SearchProvider.YOUTUBE.isActive()) {
            trackContext.setProvider(SearchProvider.YOUTUBE);
        } else {
            throw new InvalidSearchProviderException(String.format(
                "The %s search provider is disabled, and no valid fail-over providers could be found.",
                trackContext.getProvider()
            ));
        }
    }

    private boolean isRequestingYouTubeWithDirectLink() {
        return trackContext.getProvider().equals(SearchProvider.URL)
            && SearchProvider.YOUTUBE.matchesDomain(trackContext.getQuery());
    }

    @Nullable
    private AudioPlaylist loadContextFromCache() {
        SearchResultTransformer searchResult = SearchController.fetchSearchResult(trackContext);
        if (searchResult == null) {
            return null;
        }
        return searchResult.getAudioPlaylist();
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (track == null) {
            noMatches();
            return;
        }

        Metrics.tracksLoaded.inc();

        this.playlist = new BasicAudioPlaylist(track.getInfo().title, Collections.singletonList(
            track
        ), null, true);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.getTracks().isEmpty()) {
            noMatches();
            return;
        }

        Metrics.tracksLoaded.inc(playlist.getTracks().size());

        this.playlist = playlist;
    }

    @Override
    public void noMatches() {
        Metrics.trackLoadsFailed.inc();

        playlist = new BasicAudioPlaylist("No matches", Collections.emptyList(), null, true);
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        Metrics.trackLoadsFailed.inc();

        this.exception = new TrackLoadFailedException(exception);
    }
}
