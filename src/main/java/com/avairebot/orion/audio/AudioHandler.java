package com.avairebot.orion.audio;

import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class AudioHandler {

    private static final AudioPlayerManager AUDIO_PLAYER_MANAGER;
    private static final Map<Long, GuildMusicManager> MUSIC_MANAGER;

    static {
        MUSIC_MANAGER = new HashMap<>();
        AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(AUDIO_PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(AUDIO_PLAYER_MANAGER);
    }

    public static void loadAndPlay(final Message message, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());

        musicManager.setLastActiveMessage(message);

        AUDIO_PLAYER_MANAGER.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                MessageFactory.makeSuccess(message, "<@%s> has added [%s](%s) to the queue. There are `%s` song(s) ahead of it in the queue.",
                        message.getAuthor().getId(),
                        track.getInfo().title,
                        track.getInfo().uri,
                        musicManager.scheduler.getQueue().size()
                ).queue();

                play(message.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                MessageFactory.makeSuccess(message, "<@%s> has added %s songs from the [%s](%s) playlist to the queue. There are `%s` song(s) ahead of it in the queue.",
                        message.getAuthor().getId(),
                        playlist.getTracks().size(),
                        playlist.getName(),
                        trackUrl,
                        musicManager.scheduler.getQueue().size()
                ).queue();

                for (AudioTrack track : playlist.getTracks()) {
                    play(message.getGuild(), musicManager, track);
                }
            }

            @Override
            public void noMatches() {
                MessageFactory.makeWarning(message, "I found nothing with the given query `%s`", trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MessageFactory.makeError(message, "I couldn't add that to the queue: %s", exception.getMessage()).queue();
            }
        });
    }

    public static void skipTrack(Message message) {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.scheduler.nextTrack();
    }

    private static void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    private static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MUSIC_MANAGER.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(AUDIO_PLAYER_MANAGER);
            MUSIC_MANAGER.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
}
