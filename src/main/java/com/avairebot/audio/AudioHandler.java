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

import com.avairebot.AvaIre;
import com.avairebot.audio.searcher.SearchProvider;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.permissions.Permissions;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.RoleUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AudioHandler {

    private static final AudioHandler DEFAULT_AUDIO_HANDLER = new AudioHandler(
        new HashMap<>(),
        new HashMap<>()
    );
    private static AvaIre avaire;

    public final Map<Long, GuildMusicManager> musicManagers;
    public final Map<String, AudioSession> audioSessions;
    private AudioPlayerManager playerManager;

    public AudioHandler(Map<Long, GuildMusicManager> musicManagers, Map<String, AudioSession> audioSessions) {
        this.musicManagers = musicManagers;
        this.audioSessions = audioSessions;
    }

    public static void setAvaire(AvaIre avaire) {
        AudioHandler.avaire = avaire;
    }

    public static AudioHandler getDefaultAudioHandler() {
        return DEFAULT_AUDIO_HANDLER;
    }

    public AudioPlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new AudioPlayerManagerConfiguration(avaire).get();

            playerManager.getConfiguration().setResamplingQuality(
                AudioConfiguration.ResamplingQuality.valueOf(
                    avaire.getConfig().getString("audio-quality.resampling", "medium").toUpperCase()
                )
            );

            playerManager.getConfiguration().setOpusEncodingQuality(
                avaire.getConfig().getInt("audio-quality.encoding", AudioConfiguration.OPUS_QUALITY_MAX)
            );

            if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
                playerManager.enableGcMonitoring();
            }

            playerManager.setFrameBufferDuration(1000);
            playerManager.setItemLoaderThreadPoolSize(500);

            AudioSourceManagers.registerRemoteSources(playerManager);
            AudioSourceManagers.registerLocalSource(playerManager);
        }

        return playerManager;
    }

    @CheckReturnValue
    public TrackRequest loadAndPlay(CommandMessage context, @Nonnull TrackRequestContext trackContext) {
        return new TrackRequest(getGuildAudioPlayer(context.getGuild()), context, trackContext);
    }

    public void skipTrack(CommandMessage context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context.getGuild());
        if (musicManager.getScheduler().getAudioTrackContainer() != null) {
            AudioTrackContainer container = musicManager.getScheduler().getAudioTrackContainer();

            context.makeInfo(context.i18nRaw("music.internal.songWasSkipped"))
                .set("title", container.getAudioTrack().getInfo().title)
                .set("url", container.getAudioTrack().getInfo().uri)
                .set("author", container.getRequester().getAsMention())
                .queue(success -> success.delete().queueAfter(30, TimeUnit.SECONDS, null, RestActionUtil.ignore));
        }
        musicManager.getScheduler().nextTrack();
    }

    @CheckReturnValue
    public VoiceConnectStatus play(CommandMessage context, GuildMusicManager musicManager, AudioTrack track) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(context);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(track, context.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public VoiceConnectStatus play(CommandMessage context, GuildMusicManager musicManager, AudioPlaylist playlist) {
        VoiceConnectStatus voiceConnectStatus = connectToVoiceChannel(context);
        if (voiceConnectStatus.isSuccess()) {
            musicManager.getScheduler().queue(playlist, context.getAuthor());
        }
        return voiceConnectStatus;
    }

    @CheckReturnValue
    public TrackRequestContext createTrackRequestContext(@Nullable User requester, String[] args) {
        String string = String.join(" ", args);

        if (string.startsWith("scsearch:")) {
            return new TrackRequestContext(string.substring(9, string.length()).trim(), SearchProvider.SOUNDCLOUD);
        }

        if (string.startsWith("local:")) {
            string = string.substring(6, string.length()).trim();
            if (requester != null && avaire.getBotAdmins().getUserById(requester.getIdLong()).isAdmin()) {
                return new TrackRequestContext(string.substring(6, string.length()).trim(), SearchProvider.LOCAL);
            }
        }

        try {
            new URL(string);

            return new TrackRequestContext(string.trim(), SearchProvider.URL);
        } catch (MalformedURLException ex) {
            return new TrackRequestContext(string.trim(), SearchProvider.YOUTUBE);
        }
    }

    @CheckReturnValue
    public VoiceConnectStatus connectToVoiceChannel(CommandMessage context) {
        return connectToVoiceChannel(context, false);
    }

    @CheckReturnValue
    public VoiceConnectStatus connectToVoiceChannel(CommandMessage context, boolean moveChannelIfConnected) {
        return connectToVoiceChannel(context.getGuildTransformer(), context.getMessage(), context.getMember(), moveChannelIfConnected);
    }

    @CheckReturnValue
    public VoiceConnectStatus connectToVoiceChannel(GuildTransformer guildTransformer, Message message, Member member, boolean moveChannelIfConnected) {
        VoiceChannel channel = null;
        if (guildTransformer != null) {
            String musicChannelVoice = guildTransformer.getMusicChannelVoice();
            if (musicChannelVoice != null) {
                channel = message.getGuild().getVoiceChannelById(musicChannelVoice);
            }
        }

        if (channel == null) {
            channel = member.getVoiceState().getChannel();
        }

        if (channel == null) {
            return VoiceConnectStatus.NOT_CONNECTED;
        }

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            if (!LavalinkManager.LavalinkManagerHolder.lavalink.hasConnectedNodes()) {
                return VoiceConnectStatus.NO_AVAILABLE_NODES;
            }

            VoiceConnectStatus voiceConnectStatus = canConnectToChannel(message, channel);
            if (voiceConnectStatus != null) {
                return voiceConnectStatus;
            }

            LavalinkManager.LavalinkManagerHolder.lavalink.openConnection(channel);

            return VoiceConnectStatus.CONNECTED;
        }

        AudioManager audioManager = message.getGuild().getAudioManager();
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

    @CheckReturnValue
    public VoiceConnectStatus connectToVoiceChannel(Message message, VoiceChannel channel, AudioManager audioManager) {
        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            if (!LavalinkManager.LavalinkManagerHolder.lavalink.hasConnectedNodes()) {
                return VoiceConnectStatus.NO_AVAILABLE_NODES;
            }
        }

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

    private VoiceConnectStatus canConnectToChannel(Message message, VoiceChannel channel) {
        EnumSet<Permission> permissions = message.getGuild().getMember(message.getJDA().getSelfUser()).getPermissions(channel);
        if (!permissions.contains(Permission.VOICE_CONNECT)) {
            return VoiceConnectStatus.MISSING_PERMISSIONS;
        }

        if (channel.getUserLimit() > 0 && !permissions.contains(Permission.VOICE_MOVE_OTHERS) && channel.getUserLimit() <= channel.getMembers().size()) {
            return VoiceConnectStatus.USER_LIMIT;
        }
        return null;
    }

    @CheckReturnValue
    public synchronized GuildMusicManager getGuildAudioPlayer(@Nonnull Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager == null && getPlayerManager() != null) {
            musicManager = new GuildMusicManager(avaire, guild);

            musicManagers.put(guild.getIdLong(), musicManager);
        }

        if (musicManager != null && !LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        }

        return musicManager;
    }

    @CheckReturnValue
    public int getQueueSize(GuildMusicManager manager) {
        return manager.getPlayer().getPlayingTrack() == null ?
            manager.getScheduler().getQueue().size() :
            manager.getScheduler().getQueue().size() + 1;
    }

    @CheckReturnValue
    public int getTotalListenersSize() {
        int total = 0;

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            for (Link link : LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getLinks()) {
                if (link.getState().equals(Link.State.CONNECTED) || link.getState().equals(Link.State.CONNECTING)) {
                    total++;
                }
            }

            return total;
        }

        for (GuildMusicManager manager : musicManagers.values()) {
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
    public int getTotalQueueSize() {
        int total = 0;
        for (GuildMusicManager manager : musicManagers.values()) {
            total += getQueueSize(manager);
        }
        return total;
    }

    @CheckReturnValue
    public AudioSession createAudioSession(CommandMessage context, AudioPlaylist playlist) {
        AudioSession session = new AudioSession(playlist);

        audioSessions.put(
            context.getGuild().getId() + ":" + context.getAuthor().getId(),
            session
        );

        return session;
    }

    @CheckReturnValue
    public boolean hasAudioSession(CommandMessage context) {
        return audioSessions.containsKey(context.getGuild().getId() + ":" + context.getAuthor().getId());
    }

    @CheckReturnValue
    public AudioSession getAudioSession(CommandMessage context) {
        return audioSessions.getOrDefault(context.getGuild().getId() + ":" + context.getAuthor().getId(), null);
    }

    public void removeAudioSession(CommandMessage context) {
        audioSessions.remove(context.getGuild().getId() + ":" + context.getAuthor().getId());
    }

    @CheckReturnValue
    public boolean canRunDJAction(AvaIre avaire, Message message, DJGuildLevel level) {
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
                return hasDJRole(transformer, message);

            default:
                return hasDJRole(transformer, message) || level.getLevel() < guildLevel.getLevel();
        }
    }

    private boolean hasDJRole(GuildTransformer transformer, Message message) {
        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return true;
        }

        if (transformer.getDjRole() != null) {
            Role role = message.getGuild().getRoleById(transformer.getDjRole());
            if (role != null) {
                return RoleUtil.hasRole(message.getMember(), role);
            }
        }

        for (Role role : message.getMember().getRoles()) {
            if (role.getName().equalsIgnoreCase("DJ")) {
                return true;
            }
        }
        return false;
    }
}
