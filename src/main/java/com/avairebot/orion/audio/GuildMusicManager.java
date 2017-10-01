package com.avairebot.orion.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Message;

public class GuildMusicManager {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    private boolean repeatQueue = false;
    private Message lastActiveMessage = null;

    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
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
        return new AudioPlayerSendHandler(player);
    }

    public AudioPlayer getPlayer() {
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
