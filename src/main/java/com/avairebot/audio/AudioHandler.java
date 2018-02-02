package com.avairebot.audio;

import com.avairebot.AvaIre;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.permissions.Permissions;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AudioHandler {

    public static final Map<Long, GuildMusicManager> MUSIC_MANAGER;
    public static final Map<String, AudioSession> AUDIO_SESSION;

    private static AudioPlayerManager playerManager;

    static {
        MUSIC_MANAGER = new HashMap<>();
        AUDIO_SESSION = new HashMap<>();
    }

    @CheckReturnValue
    public static AudioPlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new DefaultAudioPlayerManager();

            YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
            youtubeAudioSourceManager.configureRequests(config -> RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());

            playerManager.registerSourceManager(youtubeAudioSourceManager);
            playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
            playerManager.registerSourceManager(new BandcampAudioSourceManager());
            playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            playerManager.registerSourceManager(new VimeoAudioSourceManager());
            playerManager.registerSourceManager(new BeamAudioSourceManager());
            playerManager.registerSourceManager(new LocalAudioSourceManager());

            playerManager.getConfiguration().setResamplingQuality(
                AudioConfiguration.ResamplingQuality.MEDIUM
            );

            if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled())
                playerManager.enableGcMonitoring();
            playerManager.setFrameBufferDuration(1000);
            playerManager.setItemLoaderThreadPoolSize(500);

            AudioSourceManagers.registerRemoteSources(playerManager);
            AudioSourceManagers.registerLocalSource(playerManager);
        }

        return playerManager;
    }

    @CheckReturnValue
    public static TrackRequest loadAndPlay(Message message, @Nonnull String trackUrl) {
        return new TrackRequest(getGuildAudioPlayer(message.getGuild()), message, trackUrl);
    }

    public static void skipTrack(Message message) {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        if (musicManager.getScheduler().getAudioTrackContainer() != null) {
            AudioTrackContainer container = musicManager.getScheduler().getAudioTrackContainer();

            MessageFactory.makeInfo(message, "[:title](:url) requested by :author was skipped.")
                .set("title", container.getAudioTrack().getInfo().title)
                .set("url", container.getAudioTrack().getInfo().uri)
                .set("author", container.getRequester().getAsMention())
                .queue(success -> success.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        musicManager.getScheduler().nextTrack();
    }

    @CheckReturnValue
    public static VoiceConnectStatus play(Message message, GuildMusicManager musicManager, AudioTrack track) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(message);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(track, message.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public static VoiceConnectStatus play(Message message, GuildMusicManager musicManager, AudioPlaylist playlist) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(message);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(playlist, message.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public static VoiceConnectStatus connectToVoiceChannel(Message message) {
        return connectToVoiceChannel(message, false);
    }

    @CheckReturnValue
    public static VoiceConnectStatus connectToVoiceChannel(Message message, boolean moveChannelIfConnected) {
        VoiceChannel channel = message.getMember().getVoiceState().getChannel();
        if (channel == null) {
            return VoiceConnectStatus.NOT_CONNECTED;
        }

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            VoiceConnectStatus voiceConnectStatus = canConnectToChannel(message, channel);
            if (voiceConnectStatus != null) {
                return voiceConnectStatus;
            }

            LavalinkManager.LavalinkManagerHolder.LAVALINK.openConnection(channel);

            return VoiceConnectStatus.CONNECTED;
        }

        AudioManager audioManager = message.getGuild().getAudioManager();
        if (!audioManager.isAttemptingToConnect()) {
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

    @CheckReturnValue
    private static VoiceConnectStatus connectToVoiceChannel(Message message, VoiceChannel channel, AudioManager audioManager) {
        VoiceConnectStatus voiceConnectStatus = canConnectToChannel(message, channel);
        if (voiceConnectStatus != null) {
            return voiceConnectStatus;
        }

        try {
            audioManager.openAudioConnection(channel);
        } catch (Exception ex) {
            return VoiceConnectStatus.USER_LIMIT;
        }
        return VoiceConnectStatus.CONNECTED;
    }

    private static VoiceConnectStatus canConnectToChannel(Message message, VoiceChannel channel) {
        List<Permission> permissions = message.getGuild().getMember(message.getJDA().getSelfUser()).getPermissions(channel);
        if (!permissions.contains(Permission.VOICE_CONNECT)) {
            return VoiceConnectStatus.MISSING_PERMISSIONS;
        }

        if (channel.getUserLimit() > 0 && !permissions.contains(Permission.VOICE_MOVE_OTHERS) && channel.getUserLimit() <= channel.getMembers().size()) {
            return VoiceConnectStatus.USER_LIMIT;
        }
        return null;
    }

    @CheckReturnValue
    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MUSIC_MANAGER.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(getPlayerManager(), guild);
            musicManager.getPlayer().setVolume(50);

            MUSIC_MANAGER.put(guildId, musicManager);
        }

        if (!LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

            return musicManager;
        }

        return musicManager;
    }

    @CheckReturnValue
    public static int getQueueSize(GuildMusicManager manager) {
        return manager.getPlayer().getPlayingTrack() == null ?
            manager.getScheduler().getQueue().size() :
            manager.getScheduler().getQueue().size() + 1;
    }

    @CheckReturnValue
    public static int getTotalListenersSize() {
        int total = 0;
        for (Link link : LavalinkManager.LavalinkManagerHolder.LAVALINK.getLavalink().getLinks()) {
            if (link.getState().equals(Link.State.CONNECTED) || link.getState().equals(Link.State.CONNECTING)) {
                total++;
            }
        }
        return total;
    }

    @CheckReturnValue
    public static int getTotalQueueSize() {
        int total = 0;
        for (GuildMusicManager manager : MUSIC_MANAGER.values()) {
            total += getQueueSize(manager);
        }
        return total;
    }

    @CheckReturnValue
    public static AudioSession createAudioSession(Message message, AudioPlaylist playlist) {
        AudioSession session = new AudioSession(playlist);

        AUDIO_SESSION.put(
            message.getGuild().getId() + ":" + message.getAuthor().getId(),
            session
        );

        return session;
    }

    @CheckReturnValue
    public static boolean hasAudioSession(Message message) {
        return AUDIO_SESSION.containsKey(message.getGuild().getId() + ":" + message.getAuthor().getId());
    }

    @CheckReturnValue
    public static AudioSession getAudioSession(Message message) {
        return AUDIO_SESSION.getOrDefault(message.getGuild().getId() + ":" + message.getAuthor().getId(), null);
    }

    public static void removeAudioSession(Message message) {
        AUDIO_SESSION.remove(message.getGuild().getId() + ":" + message.getAuthor().getId());
    }

    @CheckReturnValue
    public static boolean canRunDJAction(AvaIre avaire, Message message, DJGuildLevel level) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

        if (transformer == null) {
            return level.getLevel() <= DJGuildLevel.getNormal().getLevel();
        }

        DJGuildLevel guildLevel = transformer.getDJLevel();
        if (guildLevel == null) {
            guildLevel = DJGuildLevel.getNormal();
        }

        switch (guildLevel) {
            case ALL:
                return true;

            case NONE:
                return hasDJRole(message);

            default:
                return hasDJRole(message) || level.getLevel() < guildLevel.getLevel();
        }
    }

    private static boolean hasDJRole(Message message) {
        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return true;
        }

        for (Role role : message.getMember().getRoles()) {
            if (role.getName().equalsIgnoreCase("DJ")) {
                return true;
            }
        }
        return false;
    }
}
