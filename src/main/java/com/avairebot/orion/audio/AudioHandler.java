package com.avairebot.orion.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.HashMap;
import java.util.List;
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

    public static VoiceConnectStatus play(Message message, GuildMusicManager musicManager, AudioTrack track) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(message);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(track, message.getAuthor());
        }
        return voiceConnectStatus;
    }

    public static VoiceConnectStatus connectToVoiceChannel(Message message) {
        return connectToVoiceChannel(message, false);
    }

    public static VoiceConnectStatus connectToVoiceChannel(Message message, boolean moveChannelIfConnected) {
        AudioManager audioManager = message.getGuild().getAudioManager();
        if (!audioManager.isAttemptingToConnect()) {
            VoiceChannel channel = message.getMember().getVoiceState().getChannel();
            if (channel == null) {
                return VoiceConnectStatus.NOT_CONNECTED;
            }

            if (audioManager.isConnected()) {
                if (channel.getIdLong() == audioManager.getConnectedChannel().getIdLong()) {
                    return VoiceConnectStatus.CONNECTED;
                }

                if (moveChannelIfConnected) {
                    return connectToVoiceChannel(message, channel, audioManager);
                }
                return VoiceConnectStatus.CONNECTED;
            }
            return connectToVoiceChannel(message, channel, audioManager);
        }
        return VoiceConnectStatus.CONNECTED;
    }

    private static VoiceConnectStatus connectToVoiceChannel(Message message, VoiceChannel channel, AudioManager audioManager) {
        List<Permission> permissions = message.getGuild().getMember(message.getJDA().getSelfUser()).getPermissions(channel);
        if (!permissions.contains(Permission.VOICE_CONNECT)) {
            return VoiceConnectStatus.MISSING_PERMISSIONS;
        }

        if (channel.getUserLimit() > 0 && !permissions.contains(Permission.VOICE_MOVE_OTHERS) && channel.getUserLimit() <= channel.getMembers().size()) {
            return VoiceConnectStatus.USER_LIMIT;
        }

        try {
            audioManager.openAudioConnection(channel);
        } catch (Exception ex) {
            return VoiceConnectStatus.USER_LIMIT;
        }
        return VoiceConnectStatus.CONNECTED;
    }

    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MUSIC_MANAGER.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(AUDIO_PLAYER_MANAGER);
            musicManager.getPlayer().setVolume(50);

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

    public static int getTotalQueueSize() {
        int total = 0;
        for (GuildMusicManager manager : MUSIC_MANAGER.values()) {
            total += getQueueSize(manager);
        }
        return total;
    }
}
