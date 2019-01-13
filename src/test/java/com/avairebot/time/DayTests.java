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

public class DayTests extends BaseTest {

    @Test
    public void testDayNamesMatchTheEnglishMonthNames() {
        assertEquals("Monday", Day.MONDAY.getName());
        assertEquals("Tuesday", Day.TUESDAY.getName());
        assertEquals("Wednesday", Day.WEDNESDAY.getName());
        assertEquals("Thursday", Day.THURSDAY.getName());
        assertEquals("Friday", Day.FRIDAY.getName());
        assertEquals("Saturday", Day.SATURDAY.getName());
        assertEquals("Sunday", Day.SUNDAY.getName());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testDaysCanBeFoundByTheirId() {
        assertEquals("Sunday", Day.fromId(1).getName());
        assertEquals("Monday", Day.fromId(2).getName());
        assertEquals("Tuesday", Day.fromId(3).getName());
        assertEquals("Wednesday", Day.fromId(4).getName());
        assertEquals("Thursday", Day.fromId(5).getName());
        assertEquals("Friday", Day.fromId(6).getName());
        assertEquals("Saturday", Day.fromId(7).getName());
    }

    @Test
    public void testTomorrowCanBeFoundFromDate() {
        assertEquals("Tuesday", Day.MONDAY.getTomorrow().getName());
        assertEquals("Wednesday", Day.TUESDAY.getTomorrow().getName());
        assertEquals("Thursday", Day.WEDNESDAY.getTomorrow().getName());
        assertEquals("Friday", Day.THURSDAY.getTomorrow().getName());
        assertEquals("Saturday", Day.FRIDAY.getTomorrow().getName());
        assertEquals("Sunday", Day.SATURDAY.getTomorrow().getName());
        assertEquals("Monday", Day.SUNDAY.getTomorrow().getName());
    }

    @Test
    public void testYesterdayCanBeFoundFromDate() {
        assertEquals("Sunday", Day.MONDAY.getYesterday().getName());
        assertEquals("Monday", Day.TUESDAY.getYesterday().getName());
        assertEquals("Tuesday", Day.WEDNESDAY.getYesterday().getName());
        assertEquals("Wednesday", Day.THURSDAY.getYesterday().getName());
        assertEquals("Thursday", Day.FRIDAY.getYesterday().getName());
        assertEquals("Friday", Day.SATURDAY.getYesterday().getName());
        assertEquals("Saturday", Day.SUNDAY.getYesterday().getName());
    }
}
