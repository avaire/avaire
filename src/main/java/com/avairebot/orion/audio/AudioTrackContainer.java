package com.avairebot.orion.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class AudioTrackContainer {

    private final AudioTrack audioTrack;
    private final User requester;
    private final List<Long> skips;

    public AudioTrackContainer(AudioTrack audioTrack, User requester) {
        this.audioTrack = audioTrack;
        this.requester = requester;

        skips = new ArrayList<>();
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
}
