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

package com.avairebot.blacklist;

import javax.annotation.Nullable;

public class Rate {

    /**
     * The ID of the user that the rate instance was created for.
     */
    private final long userId;

    /**
     * The current rate index for the {@link #timestamps}.
     */
    private int index;

    /**
     * The array that holds the last timestamps for when the rate
     * was hit, the array should always be the same length as
     * the {@link Ratelimit#hitLimit hit limit}.
     */
    private Long[] timestamps;

    /**
     * Creates a new rate instance for the given user ID.
     *
     * @param userId The ID of the user to create the rate instance for.
     */
    public Rate(long userId) {
        this.userId = userId;
        this.index = 0;
        this.timestamps = new Long[Ratelimit.hitLimit];
    }

    /**
     * Hits the rate, creating a timestamp for the current {@link #index}.
     */
    void hit() {
        timestamps[index++] = System.currentTimeMillis();
        if (index >= Ratelimit.hitLimit) {
            index = 0;
        }
    }

    /**
     * Gets the amount of valid hits by comparing the time from the {@link #timestamps}
     * and the {@link Ratelimit#hitTime rate limit time}.
     *
     * @return The amount of valid hits currently in the rate instance.
     */
    int getHits() {
        int hits = 0;
        for (Long time : timestamps) {
            if (time != null && (time + Ratelimit.hitTime) > System.currentTimeMillis()) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * Gets the last timestamp for the current rate instance.
     *
     * @return Possibly-null, the last timestamp for the current rate.
     */
    @Nullable
    Long getLast() {
        int i = index - 1;
        if (i < 0) {
            i = Ratelimit.hitLimit - 1;
        }
        return timestamps[i];
    }

    @Override
    public int hashCode() {
        return Long.hashCode(userId);
    }
}
