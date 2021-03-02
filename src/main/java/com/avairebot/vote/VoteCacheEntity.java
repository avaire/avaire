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

import javax.annotation.Nonnull;
import java.util.Objects;

public class VoteCacheEntity {

    private final long userId;
    private int votePoints;
    private boolean optIn;
    private Carbon carbon;

    /**
     * Creates a new vote cache entity for the given user ID with
     * the given carbon instance for the expiration date.
     *
     * @param userId The ID of the user that the vote cache entity should be linked to.
     * @param carbon The carbon instance representing when their vote expires.
     */
    public VoteCacheEntity(long userId, @Nonnull Carbon carbon) {
        this(userId, 0, true, carbon);
    }

    /**
     * Creates a new vote cache entity for the given user ID, with
     * the given amount of vote points, opt-in value, and carbon
     * instance for the expiration date.
     *
     * @param userId     The ID of the user that the vote cache entity should be linked to.
     * @param votePoints The amount of vote points the user has.
     * @param optIn      The users opt-in setting for getting direct messages when they vote.
     * @param carbon     The carbon instance representing when their vote expires.
     */
    public VoteCacheEntity(long userId, int votePoints, boolean optIn, @Nonnull Carbon carbon) {
        this.userId = userId;
        this.votePoints = votePoints;
        this.optIn = optIn;
        this.carbon = carbon;
    }

    /**
     * Gets the ID of the user that the vote cache entity belongs to.
     *
     * @return The ID of the user that the vote cache entity belongs to.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the amount of vote points for the current vote cache entity.
     *
     * @return The amount of vote points the current vote cache entity has.
     */
    public int getVotePoints() {
        return votePoints;
    }

    /**
     * Sets the amount of vote points that the vote cache
     * entity for the current user should have has.
     *
     * @param votePoints The amount of vote points that the vote cache entity should have.
     */
    public void setVotePoints(int votePoints) {
        this.votePoints = votePoints;
    }

    /**
     * Checks if the user for the current cache entity is opt-in
     * for receiving vote messages through their DMs.
     *
     * @return <code>True</code> if the user is opt-in for receiving
     * vote messages, <code>False</code> otherwise.
     */
    public boolean isOptIn() {
        return optIn;
    }

    /**
     * Sets the opt-in value for receiving vote related messages through
     * direct messages, if set to false the user should be except
     * from receiving messages when they vote for the bot.
     *
     * @param optIn The opt-in value for the current cache entity user.
     */
    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    /**
     * Gets the carbon instance used to determine
     * when the vote cache entity has expired.
     *
     * @return The carbon instance for the cache entity.
     */
    public Carbon getCarbon() {
        return carbon;
    }

    /**
     * Sets the carbon instance used to determine when the vote cache entity has
     * expired, by default this should be set to 12 hours into the future.
     * <p>
     * <u>Example</u>
     * <pre><code>
     *   Carbon.now().addHours(12);
     * </code></pre>
     *
     * @param carbon The carbon instance representing when the vote cache entity expires.
     */
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
