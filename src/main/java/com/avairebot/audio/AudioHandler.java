package com.avairebot.audio;

import com.avairebot.AvaIre;
import com.avairebot.audio.source.HttpSourceManager;
import com.avairebot.audio.source.PlaylistImportSourceManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
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
    private static AvaIre avaire = null;

    static {
        MUSIC_MANAGER = new HashMap<>();
        AUDIO_SESSION = new HashMap<>();
    }

    public static void setGlobalAvaIreInstance(AvaIre avaire) {
        AudioHandler.avaire = avaire;
    }

    @CheckReturnValue
    public static AudioPlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = registerSourceManagers(new DefaultAudioPlayerManager());

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

    public static AudioPlayerManager registerSourceManagers(AudioPlayerManager manager) {
        manager.registerSourceManager(new PlaylistImportSourceManager());

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
        youtubeAudioSourceManager.configureRequests(config -> RequestConfig.copy(config)
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build());

        manager.registerSourceManager(youtubeAudioSourceManager);
        manager.registerSourceManager(new SoundCloudAudioSourceManager());
        manager.registerSourceManager(new BandcampAudioSourceManager());
        manager.registerSourceManager(new TwitchStreamAudioSourceManager());
        manager.registerSourceManager(new VimeoAudioSourceManager());
        manager.registerSourceManager(new BeamAudioSourceManager());
        manager.registerSourceManager(new LocalAudioSourceManager());
        manager.registerSourceManager(new HttpSourceManager());

        return manager;
    }

    @CheckReturnValue
    public static TrackRequest loadAndPlay(CommandMessage context, @Nonnull String trackUrl) {
        return new TrackRequest(getGuildAudioPlayer(context.getGuild()), context, trackUrl);
    }

    public static void skipTrack(CommandMessage context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context.getGuild());
        if (musicManager.getScheduler().getAudioTrackContainer() != null) {
            AudioTrackContainer container = musicManager.getScheduler().getAudioTrackContainer();

            context.makeInfo(context.i18nRaw("music.internal.songWasSkipped"))
                .set("title", container.getAudioTrack().getInfo().title)
                .set("url", container.getAudioTrack().getInfo().uri)
                .set("author", container.getRequester().getAsMention())
                .queue(success -> success.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        musicManager.getScheduler().nextTrack();
    }

    @CheckReturnValue
    public static VoiceConnectStatus play(CommandMessage context, GuildMusicManager musicManager, AudioTrack track) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(context);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(track, context.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public static VoiceConnectStatus play(CommandMessage context, GuildMusicManager musicManager, AudioPlaylist playlist) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(context);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(playlist, context.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public static VoiceConnectStatus connectToVoiceChannel(CommandMessage context) {
        return connectToVoiceChannel(context, false);
    }

    @CheckReturnValue
    public static VoiceConnectStatus connectToVoiceChannel(CommandMessage context, boolean moveChannelIfConnected) {
        VoiceChannel channel = context.getMember().getVoiceState().getChannel();
        if (channel == null) {
            return VoiceConnectStatus.NOT_CONNECTED;
        }

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            VoiceConnectStatus voiceConnectStatus = canConnectToChannel(context, channel);
            if (voiceConnectStatus != null) {
                return voiceConnectStatus;
            }

            LavalinkManager.LavalinkManagerHolder.LAVALINK.openConnection(channel);

            return VoiceConnectStatus.CONNECTED;
        }

        AudioManager audioManager = context.getGuild().getAudioManager();
        if (!audioManager.isAttemptingToConnect()) {
            if (audioManager.isConnected()) {
                if (channel.getIdLong() == audioManager.getConnectedChannel().getIdLong()) {
                    return VoiceConnectStatus.CONNECTED;
                }

                if (moveChannelIfConnected) {
                    return connectToVoiceChannel(context, channel, audioManager);
                }
                return VoiceConnectStatus.CONNECTED;
            }
            return connectToVoiceChannel(context, channel, audioManager);
        }
        return VoiceConnectStatus.CONNECTED;
    }

    @CheckReturnValue
    private static VoiceConnectStatus connectToVoiceChannel(CommandMessage context, VoiceChannel channel, AudioManager audioManager) {
        VoiceConnectStatus voiceConnectStatus = canConnectToChannel(context, channel);
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

    private static VoiceConnectStatus canConnectToChannel(CommandMessage context, VoiceChannel channel) {
        List<Permission> permissions = context.getGuild().getMember(context.getJDA().getSelfUser()).getPermissions(channel);
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
            prepareDefaultVolume(musicManager, guild);

            MUSIC_MANAGER.put(guildId, musicManager);
        }

        if (!LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
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

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            for (Link link : LavalinkManager.LavalinkManagerHolder.LAVALINK.getLavalink().getLinks()) {
                if (link.getState().equals(Link.State.CONNECTED) || link.getState().equals(Link.State.CONNECTING)) {
                    total++;
                }
            }

            return total;
        }

        for (GuildMusicManager manager : MUSIC_MANAGER.values()) {
            if (manager.getLastActiveMessage() == null) {
                continue;
            }

            AudioManager audioManager = manager.getLastActiveMessage().getGuild().getAudioManager();
            if (audioManager.isConnected() || audioManager.isAttemptingToConnect()) {
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
    public static AudioSession createAudioSession(CommandMessage context, AudioPlaylist playlist) {
        AudioSession session = new AudioSession(playlist);

        AUDIO_SESSION.put(
            context.getGuild().getId() + ":" + context.getAuthor().getId(),
            session
        );

        return session;
    }

    @CheckReturnValue
    public static boolean hasAudioSession(CommandMessage context) {
        return AUDIO_SESSION.containsKey(context.getGuild().getId() + ":" + context.getAuthor().getId());
    }

    @CheckReturnValue
    public static AudioSession getAudioSession(CommandMessage context) {
        return AUDIO_SESSION.getOrDefault(context.getGuild().getId() + ":" + context.getAuthor().getId(), null);
    }

    public static void removeAudioSession(CommandMessage context) {
        AUDIO_SESSION.remove(context.getGuild().getId() + ":" + context.getAuthor().getId());
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

    private static void prepareDefaultVolume(GuildMusicManager manager, Guild guild) {
        if (avaire == null) {
            manager.getPlayer().setVolume(50);
            return;
        }

        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        manager.getPlayer().setVolume(
            transformer != null ? transformer.getDefaultVolume() : 50
        );
    }
}
