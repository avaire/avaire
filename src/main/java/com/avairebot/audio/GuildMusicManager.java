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
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.scheduler.ScheduleHandler;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.entities.Guild;

import java.util.concurrent.TimeUnit;

public class GuildMusicManager {

    protected final AvaIre avaire;
    protected final Guild guild;

    private final IPlayer player;
    private final TrackScheduler scheduler;

    private boolean hasSetVolume;
    private boolean hasPlayedSongBefore;
    private boolean repeatQueue;
    private int defaultVolume;
    private CommandMessage lastActiveMessage = null;

    public GuildMusicManager(AvaIre avaire, Guild guild) {
        this.avaire = avaire;
        this.guild = guild;

        player = LavalinkManager.LavalinkManagerHolder.lavalink.createPlayer(guild.getId());
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
        hasSetVolume = false;
        hasPlayedSongBefore = false;
        defaultVolume = 100;

        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        defaultVolume = transformer != null ? transformer.getDefaultVolume() : 100;
    }

    public CommandMessage getLastActiveMessage() {
        return lastActiveMessage;
    }

    public void setLastActiveMessage(CommandMessage lastActiveMessage) {
        this.lastActiveMessage = lastActiveMessage;
    }

    public GuildTransformer getGuild() {
        return GuildController.fetchGuild(avaire, guild);
    }

    public IPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public boolean isRepeatQueue() {
        return repeatQueue;
    }

    public void setRepeatQueue(boolean repeatQueue) {
        this.repeatQueue = repeatQueue;
    }

    public boolean hasPlayedSongBefore() {
        return hasPlayedSongBefore;
    }

    public void setHasPlayedSongBefore(boolean hasPlayedSongBefore) {
        this.hasPlayedSongBefore = hasPlayedSongBefore;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void registerDefaultVolume() {
        if (!hasSetVolume) {
            hasSetVolume = true;

            ScheduleHandler.getScheduler().schedule(() -> {
                getPlayer().setVolume(defaultVolume);
            }, 1000, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isReady() {
        return getScheduler() != null
            && getPlayer() != null
            && getGuild() != null;
    }

    AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler((LavaplayerPlayerWrapper) player);
    }
}
