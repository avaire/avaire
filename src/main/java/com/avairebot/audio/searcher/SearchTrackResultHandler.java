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

package com.avairebot.audio.searcher;

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
import com.sedmelluq.lava.extensions.youtuberotator.tools.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SearchTrackResultHandler implements AudioLoadResultHandler {

    private static final Logger log = LoggerFactory.getLogger(SearchTrackResultHandler.class);
    private static final long defaultYouTubeCooldown = TimeUnit.MINUTES.toMillis(10);
    private static final long defaultTimeout = 3000L;
    private static long youtubeCooldownUntil = 0;

    private final TrackRequestContext trackContext;
    private boolean skipCache = false;

    private Exception exception;
    private AudioPlaylist playlist;

    /**
     * Creates a new search track result handler for the given track context.
     *
     * @param trackContext The track request context that should be used for the search.
     */
    public SearchTrackResultHandler(TrackRequestContext trackContext) {
        this.trackContext = trackContext;
    }

    /**
     * Search for an audio playlist using the set track request context,
     * with a 3000 millisecond timeout in the current thread.
     * <p>
     * The search will automatically use the audio cache unless specified otherwise,
     * and only use search providers that are globally enabled.
     *
     * @return The playlist returned from the search request, if no result were found but no
     *         exception were thrown, an empty audio playlist will be returned instead,
     *         with no selected track, and an empty track list.
     * @throws SearchingException If an invalid search request is made, or something goes wrong while
     *                            searching for audio playlists using the given request context.
     *                            Things like searching for direct links for a search provider
     *                            that is disabled, or an exception is thrown while making
     *                            the search, causing the search to fail.
     */
    @Nonnull
    public AudioPlaylist searchSync() throws SearchingException {
        return searchSync(defaultTimeout);
    }

    /**
     * Search for an audio playlist using the set track request context in the current thread.
     * <p>
     * The search will automatically use the audio cache unless specified otherwise,
     * and only use search providers that are globally enabled.
     *
     * @param timeoutMillis The amount of time to wait before the search request times
     *                      out in milliseconds.
     * @return The playlist returned from the search request, if no result were found but no
     *         exception were thrown, an empty audio playlist will be returned instead,
     *         with no selected track, and an empty track list.
     * @throws SearchingException If an invalid search request is made, or something goes wrong while
     *                            searching for audio playlists using the given request context.
     *                            Things like searching for direct links for a search provider
     *                            that is disabled, or an exception is thrown while making
     *                            the search, causing the search to fail.
     */
    @Nonnull
    public AudioPlaylist searchSync(long timeoutMillis) throws SearchingException {
        Metrics.searchRequests.inc();

        this.validateSearchProviderIsActive();

        log.debug("Searching using the {} provider for \"{}\"", trackContext.getProvider(), trackContext.getFormattedQuery());

        if (isRequestingYouTubeWhileOnCooldown()) {
            if (isRequestingYouTubeWithDirectLink()) {
                throw new TrackLoadFailedException(new SearchingException(
                    "The YouTube rate limit have been reached, please try again in a few minutes."
                ));
            }

            trackContext.setProvider(SearchProvider.SOUNDCLOUD);
        }

        if (!skipCache) {
            AudioPlaylist playlist = loadContextFromCache();
            if (playlist != null) {
                Metrics.searchHits.labels("cache").inc();

                return playlist;
            }
        }

        try {
            AudioHandler.getDefaultAudioHandler()
                .getPlayerManager()
                .loadItem(trackContext.getFullQueryString(), this)
                .get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            exception = new SearchingException(String.format(
                "Searching provider %s for \"%s\" timed out after %sms",
                trackContext.getProvider().name(), trackContext.getQuery(), timeoutMillis
            ));
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            Metrics.searchHits.labels("exception").inc();

            if (exception.getCause() != null) {
                String messageOfCause = exception.getCause().getMessage();
                if (messageOfCause.contains("java.io.IOException: Invalid status code for search response: 503")) {
                    exception = new Http503Exception("Lavaplayer search returned a 503", exception);
                } else if (messageOfCause.contains("Loading information for a YouTube track failed.")) {
                    if (exception.getCause().getCause() != null && exception.getCause().getCause().getMessage().contains("YouTube rate limit reached")) {
                        exception = new RateLimitException(messageOfCause, exception);
                    }
                }
            }

            if (isRateLimitingException() && isRequestingYouTube()) {
                log.warn("Got a {} exception from YouTube, stopping requests to the service for {} minutes",
                    exception.getClass().getName(),
                    TimeUnit.MILLISECONDS.toMinutes(defaultYouTubeCooldown)
                );

                youtubeCooldownUntil = System.currentTimeMillis() + defaultYouTubeCooldown;
            }

            if (!(exception instanceof SearchingException)) {
                exception = new SearchingException(String.format(
                    "The %s search provider failed to query for %s with exception %s",
                    trackContext.getProvider(), trackContext.getQuery(), exception.getMessage()
                ), exception);
            }

            throw (SearchingException) exception;
        }

        if (playlist == null) {
            Metrics.searchHits.labels("exception").inc();

            throw new SearchingException(String.format("Result from provider %s for query %s is unexpectedly null",
                trackContext.getProvider(), trackContext.getQuery()
            ));
        }

        if (!skipCache) {
            SearchController.cacheSearchResult(trackContext, playlist);
        }

        Metrics.searchHits.labels(playlist.getTracks().isEmpty()
            ? "empty" : "lavaplayer-" + trackContext.getProvider().name().toLowerCase()
        ).inc();

        return playlist;
    }

    /**
     * Sets whether the cache should be used in the request or not.
     *
     * @param skipCache The value that should determine if the cache is used or not.
     * @return An instance of the current search result handler.
     */
    public SearchTrackResultHandler skipCache(boolean skipCache) {
        this.skipCache = skipCache;

        return this;
    }

    /**
     * Validates the search provider used for the search context is active, if
     * the search provider is inactive then the search should not happen,
     * and will instead throw an exception to fail.
     *
     * @throws InvalidSearchProviderException Will be thrown if the context search
     *                                        provider is disabled.
     */
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

    /**
     * Checks if the currently set exception is related
     * to being rate limited by YouTube.
     *
     * @return {@code True} if the set exception is related to being
     *         rate limited by YouTube, {@code False} otherwise.
     */
    private boolean isRateLimitingException() {
        return exception instanceof Http503Exception
            || exception instanceof RateLimitException;

    }

    /**
     * Checks if the search request is directed at YouTube by checking
     * if the search provider is using the URL provider with a direct
     * YouTube link, or if the search provider is YouTube directly.
     *
     * @return {@code True} if the search is directed at YouTube.
     */
    private boolean isRequestingYouTube() {
        return isRequestingYouTubeWithDirectLink()
            || trackContext.getProvider().equals(SearchProvider.YOUTUBE);
    }

    /**
     * Checks if the search request is directed at YouTube and if we're
     * still on a cooldown due to being rate limited.
     *
     * @return {@code True} if the request is directed at YouTube and
     *         YouTube is currently rate limiting the bot.
     */
    private boolean isRequestingYouTubeWhileOnCooldown() {
        return isRequestingYouTube() && System.currentTimeMillis() < youtubeCooldownUntil;
    }

    /**
     * Checks if the search request is going to YouTube using
     * a direct URL link instead of a search query.
     *
     * @return {@code True} if the search request is using a direct YouTube
     *         link, and not a YouTube search query.
     */
    private boolean isRequestingYouTubeWithDirectLink() {
        return trackContext.getProvider().equals(SearchProvider.URL)
            && SearchProvider.YOUTUBE.matchesDomain(trackContext.getQuery());
    }

    /**
     * Loads the audio playlist cache context from the cache if it exists,
     * this will first check the in-memory cache for the results, and if
     * they don't exist there, it will use the database cache, if the
     * request context is not stored in either, null will be
     * returned instead.
     *
     * @return The AudioPlaylist instance matching the current track context
     *         from the cache if it exists, or {@code NULL}.
     */
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
