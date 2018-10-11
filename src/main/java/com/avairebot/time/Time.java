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

package com.avairebot.time;

public enum Time {

    YEARS_PER_CENTURY(100),
    YEARS_PER_DECADE(10),
    MONTHS_PER_YEAR(12),
    WEEKS_PER_YEAR(52),
    DAYS_PER_WEEK(7),
    HOURS_PER_DAY(24),
    MINUTES_PER_HOUR(60),
    SECONDS_PER_MINUTE(60);

    private final int time;

    Time(int time) {
        this.time = time;
    }

    /**
     * Gets the time.
     *
     * @return the integer time value
     */
    public int getTime() {
        return time;
    }
}
