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

import com.avairebot.scheduler.tasks.DrainVoteQueueTask;

import javax.annotation.Nonnull;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class VoteEntity implements Delayed {

    /**
     * The default time in milliseconds that should be between vote checks that
     * are sent to the DBL API, their ratelimit allows one request a second,
     * or sixty requests every minute, so just to be on the safe side
     * we'll only send a request once every one and a half seconds.
     */
    static final long defaultDuration = 1500;

    private final long userId;
    private final long channelId;
    private long duration;

    /**
     * Creates a new vote entity for the given user ID and channel ID.
     *
     * @param userId    The ID of the user that used the vote check command.
     * @param channelId The ID of the channel that the user ran the command in.
     */
    public VoteEntity(long userId, long channelId) {
        this.userId = userId;
        this.channelId = channelId;
        setDuration(defaultDuration);
    }

    /**
     * Gets the ID of of the user that the vote entity is linked to.
     *
     * @return The ID of the user that the vote entity is linked to.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the ID of the channel that the user ran the command in to be added to the vote queue.
     *
     * @return The ID of the channel the user ran the command in.
     */
    public long getChannelId() {
        return channelId;
    }

    /**
     * Gets the timestamp in milliseconds for when the vote entity is ready to
     * be consumed by the {@link DrainVoteQueueTask drain vote queue task}.
     *
     * @return The timestamp for when the vote entity is ready to be consumed.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration in milliseconds for when the vote entity can be consumed
     * by the {@link DrainVoteQueueTask drain vote queue task}, the duration
     * will be added to the current time in milliseconds.
     *
     * @param duration The time in milliseconds before the vote entity can be consumed.
     */
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
