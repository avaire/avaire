package com.avairebot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

public class GuildMusicManager {

    private final IPlayer player;
    private final TrackScheduler scheduler;

    private boolean repeatQueue = false;
    private Message lastActiveMessage = null;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        player = LavalinkManager.LavalinkManagerHolder.LAVALINK.createPlayer(guild.getId());
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
    }

    public Message getLastActiveMessage() {
        return lastActiveMessage;
    }

    public void setLastActiveMessage(Message lastActiveMessage) {
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
}
