package com.avairebot.orion.audio;

import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class AudioHandler {

    public static final AudioPlayerManager AUDIO_PLAYER_MANAGER;
    public static final Map<Long, GuildMusicManager> MUSIC_MANAGER;

    static {
        MUSIC_MANAGER = new HashMap<>();
        AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(AUDIO_PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(AUDIO_PLAYER_MANAGER);
    }

    public static TrackRequest loadAndPlay(Message message, String trackUrl) {
        return new TrackRequest(getGuildAudioPlayer(message.getGuild()), message, trackUrl);
    }

    public static void skipTrack(Message message) {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.getScheduler().nextTrack();
    }

    public static void play(Message message, GuildMusicManager musicManager, AudioTrack track) {
        if (!connectToVoiceChannel(message)) {
            MessageFactory.makeWarning(message, "You have to be connected to a voice channel.").queue();
            return;
        }

        musicManager.getScheduler().queue(track, message.getAuthor());
    }

    public static boolean connectToVoiceChannel(Message message) {
        AudioManager audioManager = message.getGuild().getAudioManager();
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            VoiceChannel channel = message.getMember().getVoiceState().getChannel();

            if (channel == null) {
                return false;
            }

            audioManager.openAudioConnection(message.getMember().getVoiceState().getChannel());
        }
        return true;
    }

    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MUSIC_MANAGER.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(AUDIO_PLAYER_MANAGER);
            MUSIC_MANAGER.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public static int getQueueSize(GuildMusicManager manager) {
        return manager.getPlayer().getPlayingTrack() == null ?
                manager.getScheduler().getQueue().size() :
                manager.getScheduler().getQueue().size() + 1;
    }
}
