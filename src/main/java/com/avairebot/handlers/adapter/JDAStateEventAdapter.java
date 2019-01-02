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

package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.VoiceConnectStatus;
import com.avairebot.audio.cache.AudioCache;
import com.avairebot.audio.cache.AudioState;
import com.avairebot.audio.cache.TrackRequestHandler;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlayCommand;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.DatabaseEventHolder;
import com.avairebot.language.I18n;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.RoleUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JDAStateEventAdapter extends EventAdapter {

    public static final Cache<Long, Long> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(3, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(JDAStateEventAdapter.class);

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public JDAStateEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onConnectToShard(JDA jda) {
        handleAutoroleTask(jda);
        handleReconnectMusic(jda);
    }

    private void handleReconnectMusic(JDA jda) {
        log.debug("Connection to shard {} has been established, running reconnect music job to reconnect music to channels that were connected during shutdown",
            jda.getShardInfo().getShardId()
        );

        int connectedChannels = 0;
        for (AudioState state : getAudioStates()) {
            if (state == null) {
                continue;
            }

            Guild guild = jda.getGuildById(state.getGuildId());
            if (guild == null) {
                continue;
            }

            VoiceChannel voiceChannel = guild.getVoiceChannelById(state.getVoiceChannelId());
            if (voiceChannel == null) {
                continue;
            }

            long usersInVoiceChannel = voiceChannel.getMembers().stream()
                .filter(member -> !member.getUser().isBot()).count();

            if (usersInVoiceChannel == 0) {
                continue;
            }

            TextChannel textChannel = guild.getTextChannelById(state.getMessageChannelId());
            if (textChannel == null) {
                continue;
            }

            connectedChannels++;
            textChannel.sendMessage(MessageFactory.createEmbeddedBuilder()
                .setDescription(I18n.getString(guild, "music.internal.resumeMusic"))
                .build()).queue(message -> {

                VoiceConnectStatus voiceConnectStatus = AudioHandler.getDefaultAudioHandler().connectToVoiceChannel(
                    message, voiceChannel, guild.getAudioManager()
                );

                if (!voiceConnectStatus.isSuccess()) {
                    message.editMessage(MessageFactory.createEmbeddedBuilder()
                        .setColor(MessageType.WARNING.getColor())
                        .setDescription(voiceConnectStatus.getErrorMessage())
                        .build()
                    ).queue();
                    return;
                }

                List<AudioCache> audioStateTacks = new ArrayList<>();
                audioStateTacks.add(state.getPlayingTrack());
                audioStateTacks.addAll(state.getQueue());

                GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(guild);
                musicManager.setLastActiveMessage(new CommandMessage(
                    CommandHandler.getCommand(PlayCommand.class),
                    new DatabaseEventHolder(GuildController.fetchGuild(avaire, guild), null),
                    message, false, new String[0]
                ));

                int trackCounter = 0;
                for (AudioCache audioCache : audioStateTacks) {
                    if (audioCache == null) {
                        continue;
                    }

                    // Limits the track request loop to 100 tracks, some servers likes
                    // to add 1,000+ tracks to the queue, we don't want to spam
                    // YouTube or SoundClouds API as soon as we start up.
                    if (trackCounter++ > 100) {
                        break;
                    }

                    Member member = message.getGuild().getMemberById(audioCache.getRequestedBy());
                    if (member == null) {
                        continue;
                    }

                    TrackRequestHandler.sendRequest(musicManager, member, audioCache.getTrackUrl());
                }
            });

            AudioCache track = state.getPlayingTrack();

            log.debug("{} stopped playing {} with {} songs in the queue",
                guild.getId(), track == null ? "Unknown Track" : track.getTrackUrl(), state.getQueue().size()
            );
        }

        log.debug("Shard {} successfully reconnected {} music channels",
            jda.getShardInfo().getShardId(), connectedChannels
        );
    }

    private List<AudioState> getAudioStates() {
        Object rawAudioState = avaire.getCache().getAdapter(CacheType.FILE).get("audio.state");
        if (rawAudioState == null) {
            return new ArrayList<>();
        }

        return AvaIre.gson.fromJson(
            String.valueOf(rawAudioState),
            new TypeToken<List<AudioState>>() {
            }.getType()
        );
    }

    private void handleAutoroleTask(JDA jda) {
        log.debug("Connection to shard {} has been established, running autorole job to sync autoroles missed due to downtime",
            jda.getShardInfo().getShardId()
        );

        if (cache.asMap().isEmpty()) {
            populateAutoroleCache();
        }

        int updatedUsers = 0;
        long thirtyMinutesAgo = Carbon.now().subMinutes(30).getTimestamp();

        for (Guild guild : jda.getGuilds()) {
            if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                continue;
            }

            Long autoroleId = cache.getIfPresent(guild.getIdLong());
            if (autoroleId == null) {
                continue;
            }

            Role autorole = guild.getRoleById(autoroleId);
            if (autorole == null) {
                continue;
            }

            for (Member member : guild.getMembers()) {
                if (member.getJoinDate().toEpochSecond() > thirtyMinutesAgo) {
                    if (!RoleUtil.hasRole(member, autorole)) {
                        updatedUsers++;
                        guild.getController().addSingleRoleToMember(member, autorole)
                            .queue();
                    }
                }
            }
        }

        log.debug("Shard {} successfully synced {} new users autorole",
            jda.getShardInfo().getShardId(), updatedUsers
        );
    }

    private void populateAutoroleCache() {
        log.debug("No cache entries was found, populating the auto role cache");
        try {
            for (DataRow row : avaire.getDatabase().query(String.format(
                "SELECT `id`, `autorole` FROM `%s` WHERE `autorole` IS NOT NULL;", Constants.GUILD_TABLE_NAME
            ))) {
                cache.put(row.getLong("id"), row.getLong("autorole"));
            }
        } catch (SQLException e) {
            log.error("Failed to populate the autorole cache: {}", e.getMessage(), e);
        }
    }
}
