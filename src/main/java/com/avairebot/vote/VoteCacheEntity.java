/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.vote;

import com.avairebot.time.Carbon;

import java.util.Objects;

public class VoteCacheEntity {

    private final long userId;
    private int votePoints;
    private boolean optIn;
    private Carbon carbon;

    public VoteCacheEntity(long userId, Carbon carbon) {
        this(userId, 0, true, carbon);
    }

    public VoteCacheEntity(long userId, int votePoints, boolean optIn, Carbon carbon) {
        this.userId = userId;
        this.votePoints = votePoints;
        this.optIn = optIn;
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

    public boolean isOptIn() {
        return optIn;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    public Carbon getCarbon() {
        return carbon;
    }

    public void setCarbon(Carbon carbon) {
        this.carbon = carbon;
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
