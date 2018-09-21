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

package com.avairebot.audio.cache;

import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.metrics.Metrics;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;

public class TrackRequestHandler {

    /**
     * Sends an audio track request to try and load the track, if the
     * track loads successfully it will be added to the music queue
     * for the server, if the track is the first song to be added
     * to the queue, it will auto play automatically.
     *
     * @param musicManager The guild music manager for the current server.
     * @param member       The member that originally requested the given audio track.
     * @param trackUrl     The URL of the track that should be requested.
     */
    public static void sendRequest(GuildMusicManager musicManager, Member member, String trackUrl) {
        Metrics.searchRequests.inc();

        AudioHandler.getDefaultAudioHandler().getPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                if (track == null) {
                    noMatches();
                    return;
                }

                Metrics.tracksLoaded.inc();

                musicManager.registerDefaultVolume();

                if (musicManager.getPlayer().isPaused()) {
                    musicManager.getPlayer().setPaused(false);
                }

                musicManager.getScheduler().queue(track, member.getUser());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.getTracks().isEmpty()) {
                    noMatches();
                    return;
                }

                Metrics.tracksLoaded.inc(playlist.getTracks().size());

                if (trackUrl.startsWith("ytsearch:") || trackUrl.startsWith("scsearch:")) {
                    trackLoaded(playlist.getTracks().get(0));
                    return;
                }

                if (playlist.getTracks().isEmpty()) {
                    noMatches();
                    return;
                }

                musicManager.registerDefaultVolume();

                if (musicManager.getPlayer().isPaused()) {
                    musicManager.getPlayer().setPaused(false);
                }

                musicManager.getScheduler().queue(playlist, member.getUser());
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
