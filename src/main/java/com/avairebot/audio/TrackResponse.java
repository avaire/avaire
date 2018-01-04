package com.avairebot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

public class TrackResponse {

    private final GuildMusicManager musicManager;
    private final AudioItem audioItem;
    private final String trackUrl;

    public TrackResponse(GuildMusicManager musicManager, AudioItem audioItem, String trackUrl) {
        this.musicManager = musicManager;
        this.audioItem = audioItem;
        this.trackUrl = trackUrl;
    }

    public GuildMusicManager getMusicManager() {
        return musicManager;
    }

    public AudioItem getAudioItem() {
        return audioItem;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public boolean isPlaylist() {
        return getAudioItem() instanceof AudioPlaylist;
    }
}
