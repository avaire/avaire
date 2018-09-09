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
