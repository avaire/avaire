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

package com.avairebot.time;

import com.avairebot.BaseTest;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeTests extends BaseTest {

    @Test
    public void testTimeReturnsTheCorrectValues() {
        assertEquals(100, Time.YEARS_PER_CENTURY.getTime());
        assertEquals(10, Time.YEARS_PER_DECADE.getTime());
        assertEquals(12, Time.MONTHS_PER_YEAR.getTime());
        assertEquals(52, Time.WEEKS_PER_YEAR.getTime());
        assertEquals(7, Time.DAYS_PER_WEEK.getTime());
        assertEquals(24, Time.HOURS_PER_DAY.getTime());
        assertEquals(60, Time.MINUTES_PER_HOUR.getTime());
        assertEquals(60, Time.SECONDS_PER_MINUTE.getTime());
    }
}
