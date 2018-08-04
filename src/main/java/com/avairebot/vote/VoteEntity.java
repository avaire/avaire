package com.avairebot.vote;

import javax.annotation.Nonnull;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class VoteEntity implements Delayed {

    static final long defaultDuration = 1500;

    private final long userId;
    private final long channelId;
    private long duration;

    public VoteEntity(long userId, long channelId) {
        this.userId = userId;
        this.channelId = channelId;
        setDuration(defaultDuration);
    }

    public long getUserId() {
        return userId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = System.currentTimeMillis() + duration;
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        return unit.convert(duration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoteEntity && userId == ((VoteEntity) obj).getUserId();
    }

    @Override
    public int compareTo(@Nonnull Delayed obj) {
        return (int) (this.duration - ((VoteEntity) obj).getDuration());
    }

    @Override
    public String toString() {
        return "VoteRequest [duration=" + duration + ", userId=" + userId + "]";
    }
}
