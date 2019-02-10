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

package com.avairebot.level;

public class ExperienceEntity {

    private final long userId;
    private final long guildId;
    private final boolean excludeLocal;
    private int experience;

    ExperienceEntity(long userId, long guildId, int experience, boolean excludeLocal) {
        this.userId = userId;
        this.guildId = guildId;
        this.experience = experience;
        this.excludeLocal = excludeLocal;
    }

    ExperienceEntity(long userId, long guildId, int experience) {
        this(userId, guildId, experience, false);
    }

    /**
     * The ID of the user that should be updated.
     *
     * @return The ID of the user that should be updated.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * The ID of the guild that the user was on when they got the XP reward.
     *
     * @return The ID of the guild that the user was on when they got an XP reward.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * The amount of experience the user has in total for the set guild.
     *
     * @return The total amount of experience the user has for the set guild.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the amount of experience that the user should be have after
     * the experience entity is consumed and synced with the database.
     *
     * @param experience The amount of experience the user should have after
     *                   the entity is synced with the database.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Determines if the local server based XP should be excluded from
     * the update or not, if the user has reached the max amount of
     * XP, there are no idea is giving them any more XP.
     *
     * @return Determins if the local server based XP should be excluded or not.
     */
    public boolean isExcludeLocal() {
        return excludeLocal;
    }

    @Override
    public String toString() {
        return String.format("[userId:%s, guildId:%s, experience:%s]",
            userId, guildId, experience
        );
    }
}
