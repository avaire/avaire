/*
 * Copyright (c) 2019.
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

package com.avairebot.mute;

import com.avairebot.language.I18n;
import com.avairebot.time.Carbon;

import java.util.concurrent.ScheduledFuture;

public class MuteContainer {

    private final long guildId;
    private final long userId;
    private final Carbon expiresAt;
    private ScheduledFuture<?> schedule;

    MuteContainer(long guildId, long userId, Carbon expiresAt) {
        this.guildId = guildId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.schedule = null;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getUserId() {
        return userId;
    }

    public Carbon getExpiresAt() {
        return expiresAt;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public void registerSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    public boolean cancelSchedule() {
        return schedule != null && schedule.cancel(true);
    }

    public boolean isPermanent() {
        return getExpiresAt() == null;
    }

    public boolean isSame(MuteContainer container) {
        return isSame(container.getGuildId(), container.getUserId());
    }

    public boolean isSame(long guildId, long userId) {
        return getGuildId() == guildId
            && getUserId() == userId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof MuteContainer && isSame((MuteContainer) obj);
    }

    @Override
    public String toString() {
        return I18n.format("MuteContainer={guildId={0}, userId={1}, expiresAt={2}}",
            getGuildId(), getUserId(), getExpiresAt()
        );
    }
}
