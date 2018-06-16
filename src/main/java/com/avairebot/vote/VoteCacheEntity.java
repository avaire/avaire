package com.avairebot.vote;

import com.avairebot.time.Carbon;

import java.util.Objects;

public class VoteCacheEntity {

    private final long userId;
    private final Carbon carbon;
    private int votePoints;

    public VoteCacheEntity(long userId, Carbon carbon) {
        this(userId, 0, carbon);
    }

    public VoteCacheEntity(long userId, int votePoints, Carbon carbon) {
        this.userId = userId;
        this.votePoints = votePoints;
        this.carbon = carbon;
    }

    public long getUserId() {
        return userId;
    }

    public int getVotePoints() {
        return votePoints;
    }

    public void setVotePoints(int votePoints) {
        this.votePoints = votePoints;
    }

    public Carbon getCarbon() {
        return carbon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoteCacheEntity that = (VoteCacheEntity) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
