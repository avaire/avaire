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

public class MonthTests extends BaseTest {

    @Test
    public void testMonthNamesMatchTheEnglishMonthNames() {
        assertEquals("January", Month.JANUARY.getName());
        assertEquals("February", Month.FEBRUARY.getName());
        assertEquals("March", Month.MARCH.getName());
        assertEquals("April", Month.APRIL.getName());
        assertEquals("May", Month.MAY.getName());
        assertEquals("June", Month.JUNE.getName());
        assertEquals("July", Month.JULY.getName());
        assertEquals("August", Month.AUGUST.getName());
        assertEquals("September", Month.SEPTEMBER.getName());
        assertEquals("October", Month.OCTOBER.getName());
        assertEquals("November", Month.NOVEMBER.getName());
        assertEquals("December", Month.DECEMBER.getName());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testMonthsCanBeFoundByTheirId() {
        assertEquals("January", Month.fromId(0).getName());
        assertEquals("February", Month.fromId(1).getName());
        assertEquals("March", Month.fromId(2).getName());
        assertEquals("April", Month.fromId(3).getName());
        assertEquals("May", Month.fromId(4).getName());
        assertEquals("June", Month.fromId(5).getName());
        assertEquals("July", Month.fromId(6).getName());
        assertEquals("August", Month.fromId(7).getName());
        assertEquals("September", Month.fromId(8).getName());
        assertEquals("October", Month.fromId(9).getName());
        assertEquals("November", Month.fromId(10).getName());
        assertEquals("December", Month.fromId(11).getName());
    }
}
