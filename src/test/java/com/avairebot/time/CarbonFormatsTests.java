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

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarbonFormatsTests extends BaseTest {

    @Test
    public void testCarbonParsesDateTimeFormatsCorrectly() throws ParseException {
        Carbon format = Carbon.createFromFormat(Formats.DATE_TIME.getFormat(), "2019-01-13 19:37:59");
        assertEquals(2019, format.getYear());
        assertEquals(1, format.getMonth());
        assertEquals(13, format.getDay());
        assertEquals(19, format.getHour());
        assertEquals(37, format.getMinute());
        assertEquals(59, format.getSecond());
    }

    @Test
    public void testCarbonDateTimeStringIsGeneratedCorrectly() throws ParseException {
        assertEquals("2019-01-13 19:37:59", Carbon.createFromFormat(
            Formats.DATE_TIME.getFormat(), "2019-01-13 19:37:59"
        ).toDateTimeString());
    }

    @Test
    public void testCarbonParsesDateFormatsCorrectly() throws ParseException {
        Carbon format = Carbon.createFromFormat(Formats.DATE.getFormat(), "2019-01-13");
        assertEquals(2019, format.getYear());
        assertEquals(1, format.getMonth());
        assertEquals(13, format.getDay());
        assertEquals(0, format.getHour());
        assertEquals(0, format.getMinute());
        assertEquals(0, format.getSecond());
    }

    @Test
    public void testCarbonDateStringIsGeneratedCorrectly() throws ParseException {
        assertEquals("2019-01-13", Carbon.createFromFormat(
            Formats.DATE.getFormat(), "2019-01-13"
        ).toDateString());
    }

    @Test
    public void testCarbonParsesFormattedDateFormatsCorrectly() throws ParseException {
        Carbon format = Carbon.createFromFormat(Formats.FORMATTED_DATE.getFormat(), "Jan 13, 2019");
        assertEquals(2019, format.getYear());
        assertEquals(1, format.getMonth());
        assertEquals(13, format.getDay());
        assertEquals(0, format.getHour());
        assertEquals(0, format.getMinute());
        assertEquals(0, format.getSecond());
    }

    @Test
    public void testCarbonFormattedDateStringIsGeneratedCorrectly() throws ParseException {
        assertEquals("Jan 13, 2019", Carbon.createFromFormat(
            Formats.FORMATTED_DATE.getFormat(), "Jan 13, 2019"
        ).toFormattedDateString());
    }

    @Test
    public void testCarbonParsesTimeFormatsCorrectly() throws ParseException {
        Carbon format = Carbon.createFromFormat(Formats.TIME.getFormat(), "19:37:59");
        assertEquals(1970, format.getYear());
        assertEquals(1, format.getMonth());
        assertEquals(1, format.getDay());
        assertEquals(19, format.getHour());
        assertEquals(37, format.getMinute());
        assertEquals(59, format.getSecond());
    }

    @Test
    public void testCarbonTimeStringIsGeneratedCorrectly() throws ParseException {
        assertEquals("19:37:59", Carbon.createFromFormat(
            Formats.TIME.getFormat(), "19:37:59"
        ).toTimeString());
    }

    @Test
    public void testCarbonParsesDayDateTimeCorrectly() throws ParseException {
        Carbon format = Carbon.createFromFormat(Formats.DAY_DATE_TIME.getFormat(), "Sun, Jan 13, 2019 7:37 PM");
        assertEquals(2019, format.getYear());
        assertEquals(13, format.getDay());
        assertEquals(19, format.getHour());
        assertEquals(37, format.getMinute());
        assertEquals(0, format.getSecond());
    }

    @Test
    public void testCarbonDayDateTimeStringIsGeneratedCorrectly() throws ParseException {
        // Windows likes to make the "PM" lowercase, while any other distribution wants
        // to make it upper case, to make the test pass regardless of OS we're just
        // forcing it into lowercase here so we can check the end result without
        // caring about letter casing.
        assertEquals("sun, jan 13, 2019 7:37 pm", Carbon.createFromFormat(
            Formats.DAY_DATE_TIME.getFormat(), "Sun, Jan 13, 2019 7:37 PM"
        ).toDayDateTimeString().toLowerCase());
    }
}
