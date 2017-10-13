package com.avairebot.orion.audio;

import com.avairebot.orion.utilities.NumberUtil;
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
        return NumberUtil.formatTime(getPlayedTime() == 0 ? 0 : getPlayedTime() / 4);
    }

    public String getFormattedDuration() {
        return NumberUtil.formatTime(getAudioTrack().getDuration());
    }

    public String getFormattedTotalTimeLeft() {
        return NumberUtil.formatTime((getAudioTrack().getDuration()) - (getPlayedTime() == 0 ? 0 : getPlayedTime() / 4));
    }
}
