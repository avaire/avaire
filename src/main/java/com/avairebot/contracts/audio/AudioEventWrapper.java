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

package com.avairebot.contracts.audio;

import com.avairebot.audio.AudioTrackContainer;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.debug.EvalAudioEventWrapper;
import com.avairebot.handlers.events.NowPlayingEvent;
import com.avairebot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.Link;
import lavalink.client.player.IPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

public abstract class AudioEventWrapper extends EvalAudioEventWrapper {

    private static final Logger log = LoggerFactory.getLogger(AudioEventWrapper.class);

    /**
     * The audio executor service pool, the pool is used to
     * close music connections on a separate thread.
     */
    protected final ExecutorService service = Executors.newCachedThreadPool();

    /**
     * The guild music manager instance, the music guild manager
     * will be created at first when someone in a guild first
     * request music through the bot, it the main source for
     * handling everything related to music in the bot.
     */
    protected final GuildMusicManager manager;

    /**
     * The generic player interface, this is used to communicate to
     * the audio player when using either Lavalink, or Lavaplayer.
     */
    protected final IPlayer player;

    /**
     * The music queue, the queue holds all the audio
     * tracks that has been requested by users.
     */
    protected final BlockingDeque<AudioTrackContainer> queue;

    /**
     * Creates a new audio event wrapper instance.
     *
     * @param manager The guild music manager instance.
     * @param player  The generic audio player, this should either
     *                be a Lavalink or Lavaplayer audio player.
     */
    public AudioEventWrapper(GuildMusicManager manager, IPlayer player) {
        this.manager = manager;
        this.player = player;

        this.queue = new LinkedBlockingDeque<>();
    }

    /**
     * Gets the audio queue with all the tracks currently in the queue.
     *
     * @return The audio queue.
     */
    public final BlockingDeque<AudioTrackContainer> getQueue() {
        return queue;
    }

    /**
     * Checks if the given link state is destroyed,
     * or if it is being destroyed right now.
     *
     * @param state The link state that should be checked.
     * @return <code>True</code> if the given link is destroyed or
     * being destroyed, <code>False</code> otherwise.
     */
    protected boolean isNodeStateDestroyed(Link.State state) {
        return !state.equals(Link.State.DESTROYED) && !state.equals(Link.State.DESTROYING);
    }

    /**
     * Gets the volume of the audio player  is playing at.
     *
     * @return The volume that the audio player is playing at.
     */
    protected int getVolume() {
        if (manager.hasPlayedSongBefore()) {
            return player.getVolume();
        }
        manager.setHasPlayedSongBefore(true);
        return manager.getDefaultVolume();
    }

    /**
     * Sends the "Now Playing" message using the given audio track container reference.
     *
     * @param container The audio track container that should be used for the "Now Playing" message.
     */
    protected void sendNowPlaying(AudioTrackContainer container) {
        NowPlayingEvent nowPlayingEvent = new NowPlayingEvent(
            manager.getGuild().getJDA(), manager.getGuild(), container
        );

        manager.getAvaire().getEventEmitter().push(nowPlayingEvent);

        final boolean repeatSingle = manager.getRepeatState().equals(GuildMusicManager.RepeatState.SINGLE);
        if (repeatSingle && container.hasMetadataKey("has-sent-now-playing")) {
            return;
        }

        if (manager.getGuildTransformer().isMusicMessages()) {
            manager.getLastActiveMessage().makeSuccess(
                manager.getLastActiveMessage().i18nRaw("music.internal.nowPlaying")
            )
                .set("title", nowPlayingEvent.getSongTitle())
                .set("link", container.getAudioTrack().getInfo().uri)
                .set("duration", container.getFormattedDuration())
                .set("requester", container.getRequester().getAsMention())
                .set("volume", getVolume())
                .queue(ignoreSuccess -> {
                    if (repeatSingle) {
                        container.setMetadata("has-sent-now-playing", true);
                    }
                });
        }
    }

    /**
     * Handles the end of the queue using the last activity message.
     *
     * @param sendEndOfQueue Determines if the "End of the queue" message should be sent or not.
     */
    public final void handleEndOfQueueWithLastActiveMessage(boolean sendEndOfQueue) {
        handleEndOfQueue(manager.getLastActiveMessage(), sendEndOfQueue);
    }

    /**
     * Handles the end of the queue with the given command message context.
     *
     * @param context        The context of the last message that was used to either invoke
     *                       the song that was playing, or cause the end of the queue.
     * @param sendEndOfQueue Determines if the "End of the queue" message should be sent or not.
     */
    public abstract void handleEndOfQueue(@Nonnull CommandMessage context, boolean sendEndOfQueue);

    /**
     * Start the next track, stopping the current one if it is playing, if it's
     * the end of the queue the "End of the queue" message will be sent.
     */
    public final void nextTrack() {
        nextTrack(true);
    }

    /**
     * Starts the next track, stopping the current one if it is playing,
     * if it's the end of the queue the music handler will disconnect
     * and cleanup, if the <code>sendEndOfQueue</code> parameter is
     * true, the "End of queue" message will also be sent.
     *
     * @param sendEndOfQueue Determine if the "End of queue" messages should
     *                       be sent if the queue is empty as the next
     *                       track is attempted to be loaded.
     */
    public abstract void nextTrack(boolean sendEndOfQueue);

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.debug("A track exception occurred while trying to play a track, skipping to the next track: {}", exception.getMessage(), exception);

        onTrackEnd(player, track, AudioTrackEndReason.LOAD_FAILED);
    }

    @Deprecated
    public void onWebSocketClose(AudioPlayer player, int code, String reason, boolean byRemote) {
        queue.clear();

        if (manager.getLastActiveMessage() == null) {
            return;
        }

        CommandMessage context = manager.getLastActiveMessage();

        context.makeWarning(context.i18nRaw("music.internal.endedDueToWebSocketClose"))
            .queue(queueMessage -> {
                queueMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
            });

        service.submit(() -> handleEndOfQueueWithLastActiveMessage(false));
    }
}
