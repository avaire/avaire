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
import com.avairebot.contracts.audio.AudioEventWrapper;
import com.avairebot.contracts.debug.EvalContext;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.handlers.events.MusicEndedEvent;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventWrapper {

    private AudioTrackContainer audioTrackContainer;

    private EvalContext queueSize = new EvalContext() {

        @Override
        public String toString() {
            return String.valueOf(queue.size());
        }
    };

    /**
     * Creates a new track scheduler for the given guild music manager and player.
     *
     * @param manager The guild music manager.
     * @param player  The audio player this scheduler uses.
     */
    TrackScheduler(GuildMusicManager manager, IPlayer player) {
        super(manager, player);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track     The track to play or add to queue.
     * @param requester The user who requested the audio track.
     */
    public void queue(AudioTrack track, User requester) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        AudioTrackContainer container = new AudioTrackContainer(track, requester);

        if (player.getPlayingTrack() != null) {
            queue.offer(container);
            return;
        }

        if (manager.getLastActiveMessage() != null) {
            player.playTrack(track);
            audioTrackContainer = container;
            sendNowPlaying(container);
        }
    }

    /**
     * Adds the list of tracks to the queue, if the player is not playing
     * a song the first track in the list will be played.
     *
     * @param playlist  The playlist transformer for the current guild.
     * @param tracks    The list of tracks to add to the queue.
     * @param requester The user who requested the audio tracks.
     */
    public void queue(PlaylistTransformer playlist, List<AudioTrack> tracks, User requester) {
        if (tracks.isEmpty() || manager.getLastActiveMessage() == null) {
            return;
        }

        int size = tracks.size();
        AudioTrackContainer container = new AudioTrackContainer(tracks.get(0), requester);

        String message = manager.getLastActiveMessage().i18nRaw("music.PlaylistCommand.loadedPlaylist");

        if (player.getPlayingTrack() == null) {
            message = message + "\n" + manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingSong");

            tracks.remove(0);
            player.playTrack(container.getAudioTrack());
            audioTrackContainer = container;
        }

        for (AudioTrack track : tracks) {
            queue.offer(new AudioTrackContainer(track, requester));
        }

        String songTitle = container.getAudioTrack().getInfo().title;
        if (songTitle == null || songTitle.equalsIgnoreCase("Unknown Title")) {
            songTitle = container.getAudioTrack().getInfo().uri;
        }

        if (manager.getGuildTransformer().isMusicMessages()) {
            manager.getLastActiveMessage().makeSuccess(message)
                .set("title", songTitle)
                .set("link", container.getAudioTrack().getInfo().uri)
                .set("size", NumberUtil.formatNicely(size))
                .set("name", playlist.getName())
                .set("amount", size)
                .set("duration", container.getFormattedDuration())
                .set("requester", container.getRequester().getAsMention())
                .set("volume", getVolume())
                .queue();
        }
    }

    /**
     * Add the first track in the playlist to the queue or play right
     * away if nothing is in the queue, then adds the rest of the
     * tracks to the queue.
     *
     * @param playlist  The playlist of tracks to play or add to the queue.
     * @param requester The user who requested the audio track.
     */
    public void queue(AudioPlaylist playlist, User requester) {
        AudioTrack track = playlist.getTracks().get(0);
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        AudioTrackContainer container = new AudioTrackContainer(track, requester);

        if (player.getPlayingTrack() != null) {
            for (int i = 0; i < playlist.getTracks().size(); i++) {
                queue.offer(new AudioTrackContainer(playlist.getTracks().get(i), requester));
            }
            return;
        }

        if (manager.getLastActiveMessage() != null) {
            player.playTrack(track);
            audioTrackContainer = container;

            String message = manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingSong");
            if (playlist.getName() != null) {
                message = manager.getLastActiveMessage().i18nRaw("music.internal.nowPlayingPlaylist")
                    + "\n" + message;
            }

            String songTitle = container.getAudioTrack().getInfo().title;
            if (songTitle == null || songTitle.equalsIgnoreCase("Unknown Title")) {
                songTitle = container.getAudioTrack().getInfo().uri;
            }

            if (manager.getGuildTransformer().isMusicMessages()) {
                manager.getLastActiveMessage().makeSuccess(message)
                    .set("title", songTitle)
                    .set("link", container.getAudioTrack().getInfo().uri)
                    .set("playlistSize", NumberUtil.formatNicely(playlist.getTracks().size()))
                    .set("playlistName", playlist.getName())
                    .set("duration", container.getFormattedDuration())
                    .set("requester", container.getRequester().getAsMention())
                    .set("volume", getVolume())
                    .queue();
            }
        }

        for (int i = 1; i < playlist.getTracks().size(); i++) {
            queue.offer(new AudioTrackContainer(playlist.getTracks().get(i), requester));
        }
    }

    /**
     * Gets the audio track container, the audio container contains the audio track
     * that is currently being played, the requested who requested the track, and
     * all the skip list with the IDs of the users who want to skip the track.
     *
     * @return The audio track container.
     */
    @Nullable
    public AudioTrackContainer getAudioTrackContainer() {
        return audioTrackContainer;
    }

    @Override
    public void nextTrack(boolean sendEndOfQueue) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrackContainer container = queue.poll();

        if (container == null) {
            if (manager.getLastActiveMessage() == null)
                return;

            service.submit(() -> handleEndOfQueueWithLastActiveMessage(sendEndOfQueue));
            return;
        }

        audioTrackContainer = container;
        player.playTrack(container.getAudioTrack());
        if (manager.getLastActiveMessage() != null) {
            sendNowPlaying(container);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (manager.getRepeatState().equals(GuildMusicManager.RepeatState.SINGLE)) {
                queue.offerFirst(audioTrackContainer.makeClone());
            } else if (manager.getRepeatState().equals(GuildMusicManager.RepeatState.ALL)) {
                queue.offer(audioTrackContainer.makeClone());
            }
            nextTrack();
        } else if (endReason.equals(AudioTrackEndReason.FINISHED) && queue.isEmpty()) {
            if (manager.getRepeatState().equals(GuildMusicManager.RepeatState.SINGLE)) {
                queue.offerFirst(audioTrackContainer.makeClone());
                nextTrack();
            } else if (manager.getLastActiveMessage() != null) {
                service.submit(() -> handleEndOfQueueWithLastActiveMessage(true));
            }
        }
    }

    @Override
    public void handleEndOfQueue(@Nonnull CommandMessage context, boolean sendEndOfQueue) {
        MusicEndedEvent event = new MusicEndedEvent(
            context.getJDA(), context.getGuild()
        );

        manager.avaire.getEventEmitter().push(event);

        if (event.isCancelled()) {
            return;
        }

        if (sendEndOfQueue && AudioHandler.getDefaultAudioHandler().musicManagers.containsKey(context.getGuild().getIdLong())) {
            context.makeSuccess(context.i18nRaw("music.internal.queueHasEnded"))
                .queue(queueMessage -> {
                    queueMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                });
        }

        LavalinkManager.LavalinkManagerHolder.lavalink.closeConnection(context.getGuild());

        GuildMusicManager manager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());
        manager.setRepeatState(GuildMusicManager.RepeatState.LOOPOFF);
        manager.getPlayer().removeListener(this);

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            if (manager.getPlayer() instanceof LavalinkPlayer) {
                LavalinkPlayer player = (LavalinkPlayer) manager.getPlayer();

                if (player.getLink() != null && isNodeStateDestroyed(player.getLink().getState())) {
                    player.getLink().destroy();
                }
            }
        } else {
            context.getGuild().getAudioManager().setSendingHandler(null);
        }

        AudioHandler.getDefaultAudioHandler().musicManagers.remove(
            context.getGuild().getIdLong()
        );
    }
}
