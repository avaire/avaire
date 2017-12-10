package com.avairebot.orion.commands.music.playlist;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.VoiceConnectStatus;
import com.avairebot.orion.commands.music.PlaylistCommand;
import com.avairebot.orion.contracts.commands.playlist.PlaylistSubCommand;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.database.transformers.PlaylistTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

public class LoadPlaylist extends PlaylistSubCommand {

    public LoadPlaylist(Orion orion, PlaylistCommand command) {
        super(orion, command);
    }

    @Override
    public boolean onCommand(Message message, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(message);
        if (!voiceConnectStatus.isSuccess()) {
            MessageFactory.makeWarning(message, voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        AudioHandler.getGuildAudioPlayer(message.getGuild()).setLastActiveMessage(message);
        for (PlaylistTransformer.PlaylistSong song : playlist.getSongs()) {
            loadSong(message, song);
        }

        return true;
    }

    private void loadSong(Message message, PlaylistTransformer.PlaylistSong song) {
        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(AudioHandler.MUSIC_MANAGER, song.getLink(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioHandler.getGuildAudioPlayer(message.getGuild())
                    .getScheduler().queue(track, message.getAuthor());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                //
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                //
            }
        });
    }
}
