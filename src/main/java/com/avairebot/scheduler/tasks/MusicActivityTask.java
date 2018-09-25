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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.language.I18n;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MusicActivityTask implements Task {

    public final static Map<Long, Integer> missingListener = new HashMap<>();
    public final static Map<Long, Integer> emptyQueue = new HashMap<>();
    public final static Map<Long, Integer> playerPaused = new HashMap<>();

    @Override
    public void handle(AvaIre avaire) {
        if (!avaire.areWeReadyYet() || !avaire.getConfig().getBoolean("music-activity.enabled", true)) {
            return;
        }

        if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            handleLavalinkNodes(avaire);
        } else {
            handleInternalLavaplayer(avaire);
        }
    }

    private void handleInternalLavaplayer(AvaIre avaire) {
        for (JDA shard : avaire.getShardManager().getShards()) {
            Iterator<AudioManager> iterator = shard.getAudioManagers().iterator();

            try {
                while (iterator.hasNext()) {
                    AudioManager manager = iterator.next();

                    if (!manager.isConnected()) {
                        continue;
                    }

                    long guildId = manager.getGuild().getIdLong();

                    if (!AudioHandler.getDefaultAudioHandler().musicManagers.containsKey(guildId)) {
                        handleEmptyMusic(avaire, manager, null, null, guildId);
                        continue;
                    }

                    GuildMusicManager guildMusicManager = AudioHandler.getDefaultAudioHandler().musicManagers.get(guildId);

                    if (guildMusicManager.getScheduler().getQueue().isEmpty() && guildMusicManager.getPlayer().getPlayingTrack() == null) {
                        handleEmptyMusic(avaire, manager, null, guildMusicManager, guildId);
                        continue;
                    }

                    if (emptyQueue.containsKey(guildId)) {
                        emptyQueue.remove(guildId);
                    }

                    if (guildMusicManager.getPlayer().isPaused()) {
                        handlePausedMusic(avaire, manager, null, guildMusicManager, guildId);
                        continue;
                    }

                    VoiceChannel voiceChannel = manager.getConnectedChannel();

                    boolean hasListeners = false;
                    for (Member member : voiceChannel.getMembers()) {
                        if (member.getUser().isBot()) {
                            continue;
                        }

                        if (member.getVoiceState().isDeafened()) {
                            continue;
                        }

                        hasListeners = true;
                        break;
                    }

                    if (hasListeners && !manager.getGuild().getSelfMember().getVoiceState().isMuted()) {
                        missingListener.remove(guildId);
                        continue;
                    }

                    int times = missingListener.getOrDefault(guildId, 0) + 1;

                    if (times <= getValue(avaire, "missing-listeners", 5)) {
                        missingListener.put(guildId, times);
                        continue;
                    }

                    clearItems(manager, null, guildMusicManager, guildId);
                }
            } catch (Exception e) {
                AvaIre.getLogger().error("An exception occurred during music activity job: " + e.getMessage(), e);
            }
        }
    }

    private void handleLavalinkNodes(AvaIre avaire) {
        for (JdaLink link : LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getLinks()) {
            long guildId = link.getGuildIdLong();

            try {
                if (!AudioHandler.getDefaultAudioHandler().musicManagers.containsKey(guildId)) {
                    handleEmptyMusic(avaire, null, link, null, guildId);
                    continue;
                }

                GuildMusicManager guildMusicManager = AudioHandler.getDefaultAudioHandler().musicManagers.get(guildId);
                if (guildMusicManager.getLastActiveMessage() == null) {
                    continue;
                }

                if (guildMusicManager.getScheduler().getQueue().isEmpty() && guildMusicManager.getPlayer().getPlayingTrack() == null) {
                    handleEmptyMusic(avaire, null, link, guildMusicManager, guildId);
                    continue;
                }

                if (emptyQueue.containsKey(guildId)) {
                    emptyQueue.remove(guildId);
                }

                if (guildMusicManager.getPlayer().isPaused()) {
                    handlePausedMusic(avaire, null, link, guildMusicManager, guildId);
                    continue;
                }

                String channel = link.getChannel();
                if (channel == null) {
                    continue;
                }

                VoiceChannel voiceChannel = avaire.getShardManager().getVoiceChannelById(channel);

                if (voiceChannel != null) {
                    boolean hasListeners = false;
                    for (Member member : voiceChannel.getMembers()) {
                        if (member.getUser().isBot()) {
                            continue;
                        }

                        if (member.getVoiceState().isDeafened()) {
                            continue;
                        }

                        hasListeners = true;
                        break;
                    }

                    if (hasListeners && !guildMusicManager.getLastActiveMessage().getGuild().getSelfMember().getVoiceState().isMuted()) {
                        missingListener.remove(guildId);
                        continue;
                    }

                    int times = missingListener.getOrDefault(guildId, 0) + 1;

                    if (times <= getValue(avaire, "missing-listeners", 5)) {
                        missingListener.put(guildId, times);
                        continue;
                    }
                }

                clearItems(null, link, guildMusicManager, guildId);
            } catch (Exception e) {
                AvaIre.getLogger().error("An exception occurred during music activity job for ID: {} - Message: " + e.getMessage(), guildId, e);
            }
        }
    }

    private void handleEmptyMusic(AvaIre avaire, @Nullable AudioManager manager, @Nullable JdaLink link, @Nullable GuildMusicManager guildMusicManager, long guildId) {
        int times = emptyQueue.getOrDefault(guildId, 0) + 1;

        if (times <= getValue(avaire, "empty-queue-timeout", 2)) {
            emptyQueue.put(guildId, times);
            return;
        }

        clearItems(manager, link, guildMusicManager, guildId);
    }

    private void handlePausedMusic(AvaIre avaire, @Nullable AudioManager manager, @Nullable JdaLink link, @Nullable GuildMusicManager guildMusicManager, long guildId) {
        int times = playerPaused.getOrDefault(guildId, 0) + 1;

        if (times <= getValue(avaire, "paused-music-timeout", 10)) {
            playerPaused.put(guildId, times);
            return;
        }

        clearItems(manager, link, guildMusicManager, guildId);
    }

    private void clearItems(@Nullable AudioManager manager, @Nullable JdaLink link, @Nullable GuildMusicManager guildMusicManager, long guildId) {
        if (guildMusicManager != null) {
            guildMusicManager.getScheduler().getQueue().clear();
            if (link != null) {
                LavalinkSocket node = link.getNode();

                if (node != null && node.isAvailable() && !LavalinkManager.LavalinkManagerHolder.lavalink.isLinkBeingDestroyed(link)) {
                    try {
                        link.destroy();
                    } catch (NullPointerException ignored) {
                        // JDA and Lavalink will sometimes throw a null pointer exception when trying
                        // to close some web socket connection, there is no way to really deal with
                        // that outside of just catching the error when we try to disconnect
                        // so we can still clear up the server player.
                    }
                }
            }

            if (guildMusicManager.getLastActiveMessage() != null && guildMusicManager.getLastActiveMessage().getChannel().canTalk()) {
                guildMusicManager.getLastActiveMessage().makeInfo(I18n.getLocale(guildMusicManager.getGuildTransformer())
                    .getConfig().getString("music.internal.endedDueToInactivity", "The music has ended due to inactivity."))
                    .queue();
            }
        }

        missingListener.remove(guildId);
        playerPaused.remove(guildId);
        emptyQueue.remove(guildId);

        if (guildMusicManager == null) {
            if (manager != null) {
                LavalinkManager.LavalinkManagerHolder.lavalink.closeConnection(manager.getGuild());
            }

            if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
                if (manager != null) {
                    manager.getGuild().getAudioManager().setSendingHandler(null);
                }
            }

            AudioHandler.getDefaultAudioHandler().musicManagers.remove(guildId);
        } else {
            guildMusicManager.getScheduler().handleEndOfQueueWithLastActiveMessage(false);
        }
    }

    private int getValue(AvaIre avaire, String path, int def) {
        return Math.max(1, avaire.getConfig().getInt("music-activity." + path, def) * 2);
    }
}
