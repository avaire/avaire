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

package com.avairebot.utilities;

import com.avairebot.BaseTest;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NumberTests extends BaseTest {

    @Test
    public void testNumbersIsParsedCorrectly() {
        assertEquals(9, NumberUtil.parseInt("9"), "9");
        assertEquals(-2, NumberUtil.parseInt("-2"), "-2");
        assertEquals(123456789, NumberUtil.parseInt("123456789"), "123456789");
    }

    @Test
    public void testDefaultsIsUsedIfInvalidStringIsGivingToParser() {
        assertEquals(0, NumberUtil.parseInt("test"));
        assertEquals(10, NumberUtil.parseInt("test", 10));
        assertEquals(-5, NumberUtil.parseInt("test", -5));
    }

    @Test
    public void testRadixIsUsedWhenParsingNumbers() {
        assertEquals(100, NumberUtil.parseInt("100", 0, 10));
        assertEquals(81, NumberUtil.parseInt("100", 0, 9));
        assertEquals(64, NumberUtil.parseInt("100", 0, 8));
        assertEquals(49, NumberUtil.parseInt("100", 0, 7));
        assertEquals(36, NumberUtil.parseInt("100", 0, 6));
        assertEquals(25, NumberUtil.parseInt("100", 0, 5));
        assertEquals(16, NumberUtil.parseInt("100", 0, 4));
        assertEquals(9, NumberUtil.parseInt("100", 0, 3));
        assertEquals(4, NumberUtil.parseInt("100", 0, 2));
    }

    @Test
    public void testParsedTimeReturnsLengthOfTimeInMillisecondsIfValid() {
        // Test seconds
        assertEquals(1000, NumberUtil.parseTimeString("1"), "1");
        assertEquals(30000, NumberUtil.parseTimeString("30"), "30");

        // Test minutes and seconds
        assertEquals(62000, NumberUtil.parseTimeString("1:2"), "1:2");
        assertEquals(62000, NumberUtil.parseTimeString("1:02"), "1:02");
        assertEquals(80000, NumberUtil.parseTimeString("1:20"), "1:20");
        assertEquals(119000, NumberUtil.parseTimeString("1:59"), "1:59");

        // Test hours, minutes, and seconds
        assertEquals(7329000, NumberUtil.parseTimeString("2:2:9"), "2:2:9");
        assertEquals(7780000, NumberUtil.parseTimeString("2:9:40"), "2:9:40");
        assertEquals(8529000, NumberUtil.parseTimeString("2:22:9"), "2:22:9");
        assertEquals(12139000, NumberUtil.parseTimeString("3:22:19"), "3:22:19");
        assertEquals(20601000, NumberUtil.parseTimeString("5:43:21"), "5:43:21");
    }

    @Test
    public void testParsedTimeThrowsAnExceptionIfInvalidInputIsGiven() {
        assertThrows(IllegalStateException.class, () -> NumberUtil.parseTimeString("invalid format"), "invalid format");
        assertThrows(IllegalStateException.class, () -> NumberUtil.parseTimeString("60:60:60"), "60:60:60");
        assertThrows(IllegalStateException.class, () -> NumberUtil.parseTimeString("1:2:60"), "1:2:60");
        assertThrows(IllegalStateException.class, () -> NumberUtil.parseTimeString("1:60:1"), "1:60:1");
        assertThrows(IllegalStateException.class, () -> NumberUtil.parseTimeString("600:1:1"), "600:1:1");
    }

    @Test
    public void testBetweenActuallyReturnsTheValueBetweenTheValuesGiven() {
        assertEquals(50, NumberUtil.getBetween(50, 1, 100));
        assertEquals(75, NumberUtil.getBetween(50, 75, 100));
        assertEquals(25, NumberUtil.getBetween(50, 1, 25));
        assertEquals(3, NumberUtil.getBetween(3, 1, 3));
        assertEquals(2, NumberUtil.getBetween(2, 1, 3));
        assertEquals(1, NumberUtil.getBetween(1, 1, 3));
    }

    @Test
    public void testFormatTimeReturnsValidAndCorrectFormats() {
        assertEquals("00:00", NumberUtil.formatTime(1));
        assertEquals("00:00", NumberUtil.formatTime(10));
        assertEquals("00:00", NumberUtil.formatTime(100));
        assertEquals("00:01", NumberUtil.formatTime(1000));
        assertEquals("00:10", NumberUtil.formatTime(10000));
        assertEquals("01:40", NumberUtil.formatTime(100000));
        assertEquals("16:40", NumberUtil.formatTime(1000000));
        assertEquals("02:46:40", NumberUtil.formatTime(10000000));
        assertEquals("27:46:40", NumberUtil.formatTime(100000000));
        assertEquals("274:20:54", NumberUtil.formatTime(987654321));
    }

    @Test
    public void testFormatTimeReturnsLiveWhenMaxValueIsGiven() {
        assertEquals("LIVE", NumberUtil.formatTime(Long.MAX_VALUE));
    }

    @Test
    public void testForcingTwoDigits() {
        assertEquals("200", NumberUtil.forceTwoDigits(200));
        assertEquals("19", NumberUtil.forceTwoDigits(19));
        assertEquals("09", NumberUtil.forceTwoDigits(9));
    }

    @Test
    public void testIsNumericFindingNumbersCorrectly() {
        assertTrue(NumberUtil.isNumeric("1"), "1");
        assertTrue(NumberUtil.isNumeric("231"), "231");
        assertTrue(NumberUtil.isNumeric("-846"), "-846");
        assertTrue(NumberUtil.isNumeric("438.26"), "438.26");

        assertFalse(NumberUtil.isNumeric("438,626"), "438,626");
        assertFalse(NumberUtil.isNumeric("test"), "test");
        assertFalse(NumberUtil.isNumeric(""), "~empty string~");
    }
}
