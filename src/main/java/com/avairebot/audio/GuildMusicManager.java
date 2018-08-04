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

    private final IPlayer player;
    private final TrackScheduler scheduler;

    private boolean hasSetVolume;
    private boolean hasPlayedSongBefore;
    private boolean repeatQueue;
    private int defaultVolume;
    private CommandMessage lastActiveMessage = null;

    public GuildMusicManager(AvaIre avaire, Guild guild) {
        player = LavalinkManager.LavalinkManagerHolder.lavalink.createPlayer(guild.getId());
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
        hasSetVolume = false;
        hasPlayedSongBefore = false;
        defaultVolume = 50;

        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        defaultVolume = transformer != null ? transformer.getDefaultVolume() : 50;
    }

    public CommandMessage getLastActiveMessage() {
        return lastActiveMessage;
    }

    public void setLastActiveMessage(CommandMessage lastActiveMessage) {
        this.lastActiveMessage = lastActiveMessage;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler((LavaplayerPlayerWrapper) player);
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
}
