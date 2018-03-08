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

public class LoadPlaylist extends PlaylistSubCommand {

    public LoadPlaylist(AvaIre avaire, PlaylistCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(context);
        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        AudioHandler.getGuildAudioPlayer(context.getGuild()).setLastActiveMessage(context);
        for (PlaylistTransformer.PlaylistSong song : playlist.getSongs()) {
            loadSong(context, song);
        }

        return true;
    }

    private void loadSong(CommandMessage context, PlaylistTransformer.PlaylistSong song) {
        Metrics.searchRequests.inc();

        AudioHandler.getPlayerManager().loadItemOrdered(AudioHandler.MUSIC_MANAGER, song.getLink(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Metrics.tracksLoaded.inc();

                AudioHandler.getGuildAudioPlayer(context.getGuild())
                    .getScheduler().queue(track, context.getAuthor());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
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
