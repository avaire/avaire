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
        VoiceConnectStatus voiceConnectStatus = AudioHandler.getDefaultAudioHandler().connectToVoiceChannel(context);
        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        List<AudioTrack> tracks = new ArrayList<>();
        AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild()).setLastActiveMessage(context);

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

        AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild())
            .getScheduler().queue(playlist, tracks, context.getAuthor());

        AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild())
            .registerDefaultVolume();
    }

    private void loadSong(PlaylistTransformer.PlaylistSong song, final List<AudioTrack> tracks, Consumer<List<AudioTrack>> success) {
        Metrics.searchRequests.inc();

        AudioHandler.getDefaultAudioHandler().getPlayerManager().loadItemOrdered(AudioHandler.getDefaultAudioHandler().musicManagers, song.getLink(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (track == null) {
                    noMatches();
                    return;
                }

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

                if (success != null) {
                    success.accept(tracks);
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Metrics.trackLoadsFailed.inc();

                if (success != null) {
                    success.accept(tracks);
                }
            }
        });
    }
}
