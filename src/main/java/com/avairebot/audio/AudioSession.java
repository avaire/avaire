package com.avairebot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import net.dv8tion.jda.core.entities.Message;

public class AudioSession {

    private final AudioPlaylist songs;
    private final long createdAt;

    private Message message;

    AudioSession(AudioPlaylist songs) {
        this.songs = songs;
        this.createdAt = System.currentTimeMillis();
    }

    public AudioPlaylist getSongs() {
        return songs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
