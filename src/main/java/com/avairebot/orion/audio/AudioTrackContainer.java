package com.avairebot.orion.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class AudioTrackContainer {

    private final AudioTrack audioTrack;
    private final User requester;
    private final List<Long> skips;
    private int playedTime;

    public AudioTrackContainer(AudioTrack audioTrack, User requester) {
        this.audioTrack = audioTrack;
        this.requester = requester;

        skips = new ArrayList<>();
        playedTime = 0;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public User getRequester() {
        return requester;
    }

    public List<Long> getSkips() {
        return skips;
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public void incrementPlayedTime() {
        playedTime++;
    }

    public String getFormattedPlayedTime() {
        return formatTime(getPlayedTime() == 0 ? 0 : getPlayedTime() / 4);
    }

    public String getFormattedDuration() {
        return formatTime(getAudioTrack().getDuration() / 1000);
    }

    public String getFormattedTotalTimeLeft() {
        return formatTime((getAudioTrack().getDuration() / 1000) - (getPlayedTime() == 0 ? 0 : getPlayedTime() / 4));
    }

    private String formatTime(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;

        if (minutes < 1) {
            return (seconds < 10) ? "0" + seconds : "" + seconds;
        }
        return String.format("%s:%s", minutes, (seconds < 10) ? "0" + seconds : seconds);
    }
}
