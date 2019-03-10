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

import com.avairebot.exceptions.InvalidFormatException;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Carbon is the equivalent of Javas Date and Calendar utilities on steroids, the idea for
 * Carbon was give by <i>Brian Nesbitt</i> at <a href="nesbot.com">nesbot.com</a> who
 * created <a href="http://carbon.nesbot.com/">Carbon for PHP</a> first.
 * <p>
 * Carbon works as a standalone utility that runs along side AvaIre, it's used to help
 * format <code>DATETIME</code> and other date fields in the database, and gives
 * you a quick and easy way to interact with the dates and calendars.
 *
 * @author Alexis Tan
 * @version 0.9.1
 */
@SuppressWarnings({"unused", "WeakerAccess", "MagicConstant"})
public final class Carbon {

    private static final Day GLOBAL_WEEK_START_AT = Day.MONDAY;
    private static final Day GLOBAL_WEEK_END_AT = Day.SUNDAY;
    private static final List<Day> WEEKEND_DAYS = Arrays.asList(Day.SATURDAY, Day.SUNDAY);
    private static String toStringFormat = Formats.DATE_TIME.getFormat();
    private final Calendar time;
    private Day WEEK_START_AT;
    private Day WEEK_END_AT;
    private TimeZone timezone;

    /**
     * Creates a new new Carbon instance with the current date and time,
     * this is the same as the {@link Carbon#now() static now()} method.
     */
    public Carbon() {
        this.time = Calendar.getInstance(Locale.ENGLISH);

        this.WEEK_START_AT = GLOBAL_WEEK_START_AT;
        this.WEEK_END_AT = GLOBAL_WEEK_END_AT;

        this.timezone = time.getTimeZone();

        this.time.setFirstDayOfWeek(WEEK_START_AT.getId());
        this.time.setTime(Calendar.getInstance(Locale.ENGLISH).getTime());
    }

    /**
     * Attempts to create a new Carbon instance from the given time string,
     * the string must be a valid date to be parsed correctly.
     * <p>
     * The string will be parsed through the <code>SimpleDateFormat</code>'s parser.
     *
     * @param time The date string to parse.
     * @throws InvalidFormatException if the date string given doesn't match any of the supported formats
     * @see java.text.SimpleDateFormat
     */
    public Carbon(String time) throws InvalidFormatException {
        this.time = Calendar.getInstance(Locale.ENGLISH);

        this.WEEK_START_AT = GLOBAL_WEEK_START_AT;
        this.WEEK_END_AT = GLOBAL_WEEK_END_AT;

        this.timezone = this.time.getTimeZone();

        this.time.setFirstDayOfWeek(WEEK_START_AT.getId());

        for (Formats supportedFormat : Formats.values()) {
            try {
                this.time.setTime(supportedFormat.parse(time));

                return;
            } catch (ParseException ignored) {
            }
        }

        throw new InvalidFormatException("'%s' does not follow any of the supported time formats, failed to creae Carbon instance.", time);
    }

    /**
     * Attempts to create a new Carbon instance from the given time string,
     * the string must be a valid date to be parsed correctly.
     * <p>
     * The string will be parsed through the <code>SimpleDateFormat</code>'s parser.
     *
     * @param time     The date string to parse.
     * @param timezone The timezone to base the time output off
     * @throws InvalidFormatException if the date string given doesn't match any of the supported formats
     * @see java.text.SimpleDateFormat
     */
    public Carbon(String time, String timezone) throws InvalidFormatException {
        this.time = Calendar.getInstance(Locale.ENGLISH);

        this.WEEK_START_AT = GLOBAL_WEEK_START_AT;
        this.WEEK_END_AT = GLOBAL_WEEK_END_AT;

        this.timezone = TimeZone.getTimeZone(timezone);

        this.time.setFirstDayOfWeek(WEEK_START_AT.getId());

        for (Formats supportedFormat : Formats.values()) {
            try {
                this.time.setTime(supportedFormat.parse(time));

                return;
            } catch (ParseException ignored) {
            }
        }

        throw new InvalidFormatException("'%s' does not follow any of the supported time formats, failed to creae Carbon instance.", time);
    }

    /**
     * Creates a new copy of the provided carbon instance.
     *
     * @param instance the carbon instance to copy
     */
    public Carbon(Carbon instance) {
        this.time = Calendar.getInstance(Locale.ENGLISH);

        this.WEEK_START_AT = instance.WEEK_START_AT;
        this.WEEK_END_AT = instance.WEEK_END_AT;

        this.timezone = instance.getTimezone();

        this.time.setFirstDayOfWeek(WEEK_START_AT.getId());

        this.time.setTime((Date) instance.getTime().getTime().clone());
    }

    /**
     * Creates an new carbon instance from an internal method call from a date.
     *
     * @param date The date to use to create the carbon instance
     */
    private Carbon(Date date) {
        this.time = Calendar.getInstance(Locale.ENGLISH);

        this.WEEK_START_AT = GLOBAL_WEEK_START_AT;
        this.WEEK_END_AT = GLOBAL_WEEK_END_AT;

        this.timezone = time.getTimeZone();

        this.time.setFirstDayOfWeek(WEEK_START_AT.getId());

        this.time.setTime(date);
    }

    /**
     * Creates a new Carbon instance with the current date and time.
     *
     * @return a Carbon instance with the current date and time.
     */
    public static Carbon now() {
        return new Carbon();
    }

    /**
     * Creates a new Carbon instance with the current date and time.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the current date and time.
     */
    public static Carbon now(String timezone) {
        Carbon carbon = now().startOfDay();

        carbon.setTimezone(timezone);

        return carbon;
    }

    /**
     * Creates a new Carbon instance with the current date and time.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the current date and time.
     */
    public static Carbon now(TimeZone timezone) {
        Carbon carbon = now().startOfDay();

        carbon.setTimezone(timezone);

        return carbon;
    }

    /**
     * Create a new Carbon instance with the date of today at the start of the day.
     *
     * @return a Carbon instance with the date of today at the start of the day
     */
    public static Carbon today() {
        return now().startOfDay();
    }

    /**
     * Create a new Carbon instance with the date of today at the start of the day.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date of today at the start of the day
     */
    public static Carbon today(String timezone) {
        return now().startOfDay().setTimezone(timezone);
    }

    /**
     * Create a new Carbon instance with the date of today at the start of the day.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date of today at the start of the day
     */
    public static Carbon today(TimeZone timezone) {
        return now().startOfDay().setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance with the date and time set to tomorrow.
     *
     * @return a Carbon instance with the date and time set to tomorrows date.
     */
    public static Carbon tomorrow() {
        return now().addDay().startOfDay();
    }

    /**
     * Creates a new Carbon instance with the date and time set to tomorrow.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date and time set to tomorrows date.
     */
    public static Carbon tomorrow(String timezone) {
        return now().addDay().startOfDay().setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance with the date and time set to tomorrow.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date and time set to tomorrows date.
     */
    public static Carbon tomorrow(TimeZone timezone) {
        return now().addDay().startOfDay().setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance with the date and time set to yesterday.
     *
     * @return a Carbon instance with the date and time set to yesterdays date.
     */
    public static Carbon yesterday() {
        return now().subDay().startOfDay();
    }

    /**
     * Creates a new Carbon instance with the date and time set to yesterday.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date and time set to yesterdays date.
     */
    public static Carbon yesterday(String timezone) {
        return now().subDay().startOfDay().setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance with the date and time set to yesterday.
     *
     * @param timezone The timezone to base the date output off
     * @return a Carbon instance with the date and time set to yesterdays date.
     */
    public static Carbon yesterday(TimeZone timezone) {
        return now().subDay().startOfDay().setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance from the provided date information, the
     * date information is provided in the form of a varargs object.
     * <p>
     * The arguments can be formatted as followed:
     * <ol>
     * <li>Year - Can be a integer or <code>NULL</code></li>
     * <li>Month - Can be a integer or <code>NULL</code></li>
     * <li>Day of Month - Can be a integer or <code>NULL</code></li>
     * <li>TimeZone - Can be a String or <code>NULL</code></li>
     * </ol>
     * <p>
     * If <code>NULL</code> is used for any of the arguments, the current
     * date value for that filed will be used instead.
     * <p>
     * The time values will default to now.
     *
     * @param params The varargs object that contains the date information
     * @return a Carbon instance with the provided date.
     */
    public static Carbon createFromDate(Object... params) {
        Carbon carbon = new Carbon();

        if (params.length >= 1 && params[0] != null && parseObj(params[0]) > 0) {
            carbon.setYear(parseObj(params[0]));
        }

        if (params.length >= 2 && params[1] != null && parseObj(params[1]) > 0) {
            carbon.setMonth(parseObj(params[1]));
        }

        if (params.length >= 3 && params[2] != null && parseObj(params[2]) > 0) {
            carbon.setDay(parseObj(params[2]));
        }

        if (params.length >= 4 && params[3] != null) {
            return parseTimezone(carbon, params[3]);
        }

        return carbon;
    }

    /**
     * Creates a new Carbon instance from the provided time information, the
     * time information is provided in the form of a varargs object.
     * <p>
     * The arguments can be formatted as followed:
     * <ol>
     * <li>Hour - Can be a integer or <code>NULL</code></li>
     * <li>Minute - Can be a integer or <code>NULL</code></li>
     * <li>Second - Can be a integer or <code>NULL</code></li>
     * <li>TimeZone - Can be a String or <code>NULL</code></li>
     * </ol>
     * <p>
     * If <code>NULL</code> is used for any of the arguments, the current
     * time value for that filed will be used instead.
     * <p>
     * The date values will default to now.
     *
     * @param params The varargs object that contains the date information
     * @return a Carbon instance with the provided time.
     */
    public static Carbon createFromTime(Object... params) {
        Carbon carbon = new Carbon();

        if (params.length >= 1 && params[0] != null && parseObj(params[0]) >= 0) {
            carbon.setHour(parseObj(params[0]));
        }

        if (params.length >= 2 && params[1] != null && parseObj(params[1]) >= 0) {
            carbon.setMinute(parseObj(params[1]));
        }

        if (params.length >= 3 && params[2] != null && parseObj(params[2]) >= 0) {
            carbon.setSecond(parseObj(params[2]));
        }

        if (params.length >= 4 && params[3] != null) {
            return parseTimezone(carbon, params[3]);
        }

        return carbon;
    }

    /**
     * Creates a new Carbon instance from the provided offset date time object.
     *
     * @param offsetDateTime The offset date time that the Carbon instance should be created from.
     * @return a Carbon instance with the provided date and time.
     */
    public static Carbon createFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        return Carbon.create(
            offsetDateTime.getYear(),
            offsetDateTime.getMonthValue(),
            offsetDateTime.getDayOfMonth(),
            offsetDateTime.getHour(),
            offsetDateTime.getMinute(),
            offsetDateTime.getSecond()
        );
    }

    /**
     * Creates a new Carbon instance from the provided date and time information,
     * the date and time information is provided in the form of a varargs object.
     * <p>
     * The arguments can be formatted as followed:
     * <ol>
     * <li>Year - Can be a integer or <code>NULL</code></li>
     * <li>Month - Can be a integer or <code>NULL</code></li>
     * <li>Day of Month - Can be a integer or <code>NULL</code></li>
     * <li>Hour - Can be a integer or <code>NULL</code></li>
     * <li>Minute - Can be a integer or <code>NULL</code></li>
     * <li>Second - Can be a integer or <code>NULL</code></li>
     * <li>TimeZone - Can be a String or <code>NULL</code></li>
     * </ol>
     * <p>
     * If <code>NULL</code> is used for any of the arguments, the current
     * date or time value for that filed will be used instead.
     * <p>
     *
     * @param params The varargs object that contains the date and time information
     * @return a Carbon instance with the provided date and time.
     */
    public static Carbon create(Object... params) {
        Carbon carbon = new Carbon();

        if (params.length >= 1 && params[0] != null && parseObj(params[0]) > 0) {
            carbon.setYear(parseObj(params[0]));
        }

        if (params.length >= 2 && params[1] != null && parseObj(params[1]) > 0) {
            carbon.setMonth(parseObj(params[1]));
        }

        if (params.length >= 3 && params[2] != null && parseObj(params[2]) > 0) {
            carbon.setDay(parseObj(params[2]));
        }

        if (params.length >= 4 && params[3] != null && parseObj(params[3]) >= 0) {
            carbon.setHour(parseObj(params[3]));
        }

        if (params.length >= 5 && params[4] != null && parseObj(params[4]) >= 0) {
            carbon.setMinute(parseObj(params[4]));
        }

        if (params.length >= 6 && params[5] != null && parseObj(params[5]) >= 0) {
            carbon.setSecond(parseObj(params[5]));
        }

        if (params.length >= 7 && params[6] != null) {
            if (params[6] instanceof TimeZone) {
                carbon.setTimezone((TimeZone) params[6]);

                return carbon;
            }

            if (params[6] instanceof String) {
                carbon.setTimezone((String) params[6]);

                return carbon;
            }
        }

        return carbon;
    }

    /**
     * Convert a object to an integer if possible.
     *
     * @param obj The object to convert to an integer
     * @return either (1) the integer converted from the object
     * or (2) 0 if an error occurred or the object is <code>NULL</code>.
     */
    private static int parseObj(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            return (int) obj;
        } catch (ClassCastException e) {
            return 0;
        }
    }

    /**
     * Creates a new Carbon instance from the provided time string
     * and {@link java.text.SimpleDateFormat} string format, if
     * the format is invalid an {@link IllegalArgumentException} will be thrown.
     *
     * @param format The {@link java.text.SimpleDateFormat} to use
     * @param time   The time string that should be parsed
     * @return a new Carbon instance from the provided time string and format.
     * @throws ParseException           if the provided time doesn't match the provided format.
     * @throws IllegalArgumentException if the provided {@link java.text.SimpleDateFormat}
     *                                  string format is invalid.
     */
    public static Carbon createFromFormat(String format, String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);

        Date date = sdf.parse(time);

        return new Carbon(date);
    }

    /**
     * Creates a new Carbon instance from the provided time string
     * and {@link java.text.SimpleDateFormat} string format, if
     * the format is invalid an {@link IllegalArgumentException} will be thrown.
     *
     * @param format   The {@link java.text.SimpleDateFormat} to use
     * @param time     The time string that should be parsed
     * @param timezone The timezone that should be used
     * @return a new Carbon instance from the provided time string and format.
     * @throws ParseException           if the provided time doesn't match the provided format.
     * @throws IllegalArgumentException if the provided {@link java.text.SimpleDateFormat}
     *                                  string format is invalid.
     */
    public static Carbon createFromFormat(String format, String time, String timezone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);

        Date date = sdf.parse(time);

        return new Carbon(date).setTimezone(timezone);
    }

    /**
     * Creates a new Carbon instance from the provided time string
     * and {@link java.text.SimpleDateFormat} string format, if
     * the format is invalid an {@link IllegalArgumentException} will be thrown.
     *
     * @param format   The {@link java.text.SimpleDateFormat} to use
     * @param time     The time string that should be parsed
     * @param timezone The timezone that should be used
     * @return a new Carbon instance from the provided time string and format.
     * @throws ParseException           if the provided time doesn't match the provided format.
     * @throws IllegalArgumentException if the provided {@link java.text.SimpleDateFormat}
     *                                  string format is invalid.
     */
    public static Carbon createFromFormat(String format, String time, TimeZone timezone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);

        Date date = sdf.parse(time);

        return new Carbon(date).setTimezone(timezone);
    }

    /**
     * Sets the global {@link #toString() } format, by default
     * the {@link Formats#DATE_TIME date time} is used.
     *
     * @param format The format to set
     */
    public static void setToStringFormat(String format) {
        toStringFormat = format;
    }

    /**
     * Resets the global {@link #toString() } format back to the
     * default {@link Formats#DATE_TIME date time} format.
     */
    public static void resetToStringFormat() {
        toStringFormat = Formats.DATE_TIME.getFormat();
    }

    /**
     * Parses the object and sets it to a timezone for the given Carbon instance
     * if the object given matches one of the valid timezone formats.
     *
     * @param carbon The Carbon instance that should be modified.
     * @param obj    The object that should be parsed into a timezone
     * @return The modified carbon instance with the new timezone.
     */
    @Nonnull
    private static Carbon parseTimezone(Carbon carbon, Object obj) {
        if (obj instanceof TimeZone) {
            return carbon.setTimezone((TimeZone) obj);
        }
        if (obj instanceof String) {
            return carbon.setTimezone((String) obj);
        }
        return carbon;
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////// GETTERS AND SETTERS /////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Sets the given calendar field to the given value. The value is not
     * interpreted by this method regardless of the leniency mode.
     *
     * @param field the given calendar field.
     * @param value the value to be set for the given calendar field.
     * @return the Carbon instance
     * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
     *                                        (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
     */
    public Carbon set(int field, int value) {
        time.set(field, value);

        return this;
    }

    /**
     * Returns the value of the given calendar field. In lenient mode,
     * all calendar fields are normalized. In non-lenient mode, all
     * calendar fields are validated and this method throws an
     * exception if any calendar fields have out-of-range values. The
     * normalization and validation are handled by the
     * {@link java.util.Calendar#complete()} method, which process is calendar
     * system dependent.
     *
     * @param field the given calendar field.
     * @return the value for the given calendar field.
     * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
     *                                        (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
     */
    public int get(int field) {
        return time.get(field);
    }

    /**
     * Gets the second from the carbon instance.
     *
     * @return the second of the carbon instance
     */
    public int getSecond() {
        return get(Calendar.SECOND);
    }

    /**
     * Sets the second to the carbon instance.
     *
     * @param second the seconds to set
     * @return the Carbon instance
     */
    public Carbon setSecond(int second) {
        return set(Calendar.SECOND, second);
    }

    /**
     * Gets the minute from the carbon instance.
     *
     * @return the minute of the carbon instance
     */
    public int getMinute() {
        return get(Calendar.MINUTE);
    }

    /**
     * Sets the minute to the carbon instance.
     *
     * @param minute the minute to set
     * @return the Carbon instance
     */
    public Carbon setMinute(int minute) {
        return set(Calendar.MINUTE, minute);
    }

    /**
     * Gets the hour from the carbon instance.
     *
     * @return the hour of the carbon instance
     */
    public int getHour() {
        return get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Sets the hour to the carbon instance.
     *
     * @param hour the hour to set
     * @return the Carbon instance
     */
    public Carbon setHour(int hour) {
        return set(Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * Gets the day of the month from the carbon instance.
     *
     * @return the day of the carbon instance
     */
    public int getDay() {
        return get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Sets the day of the month to the carbon instance.
     *
     * @param day the day to set
     * @return the Carbon instance
     */
    public Carbon setDay(int day) {
        return set(Calendar.DAY_OF_MONTH, day);
    }

    /**
     * Sets the day of the week, this is a calendar-specific value.
     * The days count from 1 to 7, where 1 is a {@link Day#SUNDAY}
     *
     * @param day The day to set
     * @return the Carbon instance
     */
    public Carbon setDayOfWeek(int day) {
        return set(Calendar.DAY_OF_WEEK, day);
    }

    /**
     * Gets the day of the week.
     *
     * @return the Day object that match the current day of the week.
     * @see Day
     */
    public Day getDayOfWeek() {
        return Day.fromId(get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Sets the day of the week, this is a calendar-specific value.
     * The days count from 1 to 7, where 1 is a {@link Day#SUNDAY}
     *
     * @param day The day to set
     * @return the Carbon instance
     */
    public Carbon setDayOfWeek(Day day) {
        return set(Calendar.DAY_OF_WEEK, day.getId());
    }

    /**
     * Gets the week from the carbon instance.
     *
     * @return the week of the carbon instance
     */
    public int getWeek() {
        return get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Sets the week to the carbon instance.
     * <p>
     * This is a calendar-specific value. The week starts on a <code>MONDAY</code> and ends on a <code>SUNDAY</code>
     *
     * @param week the week to set
     * @return the Carbon instance
     * @see #WEEK_START_AT
     * @see #WEEK_END_AT
     */
    public Carbon setWeek(int week) {
        return set(Calendar.WEEK_OF_MONTH, week);
    }

    /**
     * Gets the month from the carbon instance.
     *
     * @return the month of the carbon instance
     */
    public int getMonth() {
        return get(Calendar.MONTH) + 1;
    }

    /**
     * Sets the month to the carbon instance.
     * <p>
     * This is a calendar-specific value. The first month of the year in the
     * Gregorian and Julian calendars is <code>JANUARY</code> which is 0;
     * the last depends on the number of months in a year.
     *
     * @param month the month to set
     * @return the Carbon instance
     * @see java.util.Calendar#JANUARY
     * @see java.util.Calendar#FEBRUARY
     * @see java.util.Calendar#MARCH
     * @see java.util.Calendar#APRIL
     * @see java.util.Calendar#MAY
     * @see java.util.Calendar#JUNE
     * @see java.util.Calendar#JULY
     * @see java.util.Calendar#AUGUST
     * @see java.util.Calendar#SEPTEMBER
     * @see java.util.Calendar#OCTOBER
     * @see java.util.Calendar#NOVEMBER
     * @see java.util.Calendar#DECEMBER
     * @see java.util.Calendar#UNDECIMBER
     */
    public Carbon setMonth(int month) {
        return set(Calendar.MONTH, month - 1);
    }

    /**
     * Sets the month to the carbon instance.
     *
     * @param month the month to set
     * @return the Carbon instance
     */
    public Carbon setMonth(Month month) {
        return set(Calendar.MONTH, month.getId());
    }

    /**
     * Gets the year from the carbon instance.
     *
     * @return the year of the carbon instance
     */
    public int getYear() {
        return get(Calendar.YEAR);
    }

    /**
     * Sets the year to the carbon instance.
     *
     * @param year the year to set
     * @return the Carbon instance
     */
    public Carbon setYear(int year) {
        return set(Calendar.YEAR, year);
    }

    /**
     * Gets the day of year from the carbon instance.
     *
     * @return the day of the year of the carbon instance.
     */
    public int getDayOfYear() {
        return get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Gets the week of the month from the carbon instance.
     *
     * @return the week of the month of the carbon instance.
     */
    public int getWeekOfMonth() {
        return get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Gets the week of year from the carbon instance.
     *
     * @return the week of the year of the carbon instance.
     */
    public int getWeekOfYear() {
        return get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Gets the numeric value of how many days there are in the current month.
     *
     * @return the numeric value of how many days there are in the current month.
     */
    public int getDaysInMonth() {
        return time.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Gets the current time since epoch in seconds.
     *
     * @return the current time since epoch in seconds.
     */
    public long getTimestamp() {
        return time.getTimeInMillis() / 1000;
    }

    /**
     * Sets a timestamp to the current date and time, the timestamp should be in seconds.
     *
     * @param timestamp the time in seconds since epoch.
     * @return the Carbon instance with the new date and time.
     */
    public Carbon setTimestamp(long timestamp) {
        time.setTime(new Date(timestamp * 1000));

        return this;
    }

    /**
     * Gets the current quarter of the year.
     *
     * @return the current quarter of the year.
     */
    public int getQuarter() {
        return (get(Calendar.MONTH) / 3) + 1;
    }

    /**
     * Gets the age of the Carbon instance, it's just a fancy way of getting
     * the difference between now and the carbon instances time.
     * <p>
     * <strong>Note:</strong> The method will always return a positive integer,
     * even when the carbon instance is set into the future.
     *
     * @return the age of the carbon instance.
     */
    public int getAge() {
        Carbon carbon = new Carbon();

        return getPositive(getYear() - (carbon.setTimezone(timezone)).getYear());
    }

    /**
     * Gets the calendar object used by carbon.
     *
     * @return the calendar object used by carbon
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * Sets the current timezone used to format the datetime outputs,
     * if <code>NULL</code> parsed nothing will be set.
     *
     * @param timezone the timezone to use
     * @return the Carbon instance.
     */
    public Carbon setTimezone(TimeZone timezone) {
        if (timezone != null) {
            this.timezone = timezone;
        }

        return this;
    }

    /**
     * Gets the current timezone used to format the datetime outputs.
     *
     * @return the current timezone used to format the datetime outputs.
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Sets the current timezone used to format the datetime outputs,
     * if <code>NULL</code> parsed nothing will be set.
     *
     * @param timezone the timezone to use
     * @return the Carbon instance.
     */
    public Carbon setTimezone(String timezone) {
        return setTimezone(TimeZone.getTimeZone(timezone));
    }

    /**
     * Sets the first day of the week, used by {@link #startOfWeek() } to
     * set the date to the first day of the week.
     * <p>
     * The last day of the week will also be set using the {@link Day#getYesterday() } method.
     *
     * @param day The day to set as the first day of the week
     * @return the Carbon instance.
     */
    public Carbon setFirstDayOfWeek(Day day) {
        WEEK_START_AT = day;
        WEEK_END_AT = day.getYesterday();

        return this;
    }

    /**
     * Sets the last day of the week, used by {@link #endOfWeek() } to
     * set the date to the last day of the week.
     * <p>
     * The first day of the week will also be set using the {@link Day#getTomorrow() } method.
     *
     * @param day The day to set as the last day of the week.
     * @return the Carbon instance.
     */
    public Carbon setLastDayOfWeek(Day day) {
        WEEK_END_AT = day;
        WEEK_START_AT = day.getTomorrow();

        return this;
    }

    /**
     * Sets the carbon instances date to the provided year, month a day.
     *
     * @param year  The year to set
     * @param month The month to set
     * @param day   The day to set
     * @return the Carbon instance.
     */
    public Carbon setDate(int year, int month, int day) {
        return setYear(year).setMonth(month).setDay(day);
    }

    /**
     * Sets the carbon instance time to the provided hour, minute and second.
     *
     * @param hour   The hour to set
     * @param minute The minute to set
     * @param second The second to set
     * @return the Carbon instance.
     */
    public Carbon setTime(int hour, int minute, int second) {
        return setHour(hour).setMinute(minute).setSecond(second);
    }

    /**
     * Sets the carbon instances date and time to the provided year, month, day, hour, minute and second.
     *
     * @param year   The year to set
     * @param month  The month to set
     * @param day    The day to set
     * @param hour   The hour to set
     * @param minute The minute to set
     * @param second The second to set
     * @return the Carbon instance.
     */
    public Carbon setDateTime(int year, int month, int day, int hour, int minute, int second) {
        return setDate(year, month, day).setTime(hour, minute, second);
    }

    /**
     * Adds the specified amount of time to the given calendar field,
     * based on the calendar's rules. For example, to add 5 days to
     * the current time of the calendar, you can achieve it by calling:
     * <p>
     * <code>add(Calendar.DAY_OF_MONTH, 5)</code>.
     *
     * @param field  the calendar field.
     * @param amount the amount of date or time to be added to the field.
     * @return the Carbon instance.
     */
    public Carbon add(int field, int amount) {
        time.add(field, getPositive(amount));

        return this;
    }

    /**
     * Subtracts the specified amount of time from the given calendar field,
     * based on the calendar's rules. For example, to subtract 5 days from
     * the current time of the calendar, you can achieve it by calling:
     * <p>
     * <code>sub(Calendar.DAY_OF_MONTH, 5)</code>.
     *
     * @param field  the calendar field.
     * @param amount the amount of date or time to be added to the field.
     * @return the Carbon instance.
     */
    public Carbon sub(int field, int amount) {
        time.add(field, getNegative(amount));

        return this;
    }

    ///////////////////////////////////////////////////////////////////
    /////////////////// ADDITIONS AND SUBTRACTIONS ////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Adds one second to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addSecond() {
        return addSeconds(1);
    }

    /**
     * Adds the given amount of seconds to the carbon instance.
     *
     * @param seconds the amount of seconds to add
     * @return the Carbon instance
     */
    public Carbon addSeconds(int seconds) {
        return add(Calendar.SECOND, seconds);
    }

    /**
     * Subtracts one second from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subSecond() {
        return subSeconds(1);
    }

    /**
     * Subtracts the given amount of seconds from the carbon instance.
     *
     * @param seconds the amount of seconds to subtract
     * @return the Carbon instance
     */
    public Carbon subSeconds(int seconds) {
        return sub(Calendar.SECOND, seconds);
    }

    /**
     * Adds one minute to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addMinute() {
        return addMinutes(1);
    }

    /**
     * Adds the given amount of minutes to the carbon instance.
     *
     * @param minutes the amount of minutes to add
     * @return the Carbon instance
     */
    public Carbon addMinutes(int minutes) {
        return add(Calendar.MINUTE, minutes);
    }

    /**
     * Subtracts one minute from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subMinute() {
        return subMinutes(1);
    }

    /**
     * Subtracts the given amount of minutes from the carbon instance.
     *
     * @param minutes the amount of minutes to subtract
     * @return the Carbon instance
     */
    public Carbon subMinutes(int minutes) {
        return sub(Calendar.MINUTE, minutes);
    }

    /**
     * Adds one hour to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addHour() {
        return addHours(1);
    }

    /**
     * Adds the given amount of hours to the carbon instance.
     *
     * @param hours the amount of hours to add
     * @return the Carbon instance
     */
    public Carbon addHours(int hours) {
        return add(Calendar.HOUR, hours);
    }

    /**
     * Subtracts one hour from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subHour() {
        return subHours(1);
    }

    /**
     * Subtracts the given amount of hours from the carbon instance.
     *
     * @param hours the amount of hours to subtract
     * @return the Carbon instance
     */
    public Carbon subHours(int hours) {
        return sub(Calendar.HOUR, hours);
    }

    /**
     * Adds one day to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addDay() {
        return addDays(1);
    }

    /**
     * Adds the given amount of days to the carbon instance.
     *
     * @param days the amount of days to add
     * @return the Carbon instance
     */
    public Carbon addDays(int days) {
        return add(Calendar.DAY_OF_MONTH, days);
    }

    /**
     * Subtracts one day from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subDay() {
        return subDays(1);
    }

    /**
     * Subtracts the given amount of days from the carbon instance.
     *
     * @param days the amount of days to subtract
     * @return the Carbon instance
     */
    public Carbon subDays(int days) {
        return sub(Calendar.DAY_OF_MONTH, days);
    }

    /**
     * Adds one week to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addWeek() {
        return addWeeks(1);
    }

    /**
     * Adds the given amount of weeks to the carbon instance.
     *
     * @param weeks the amount of weeks to add
     * @return the Carbon instance
     */
    public Carbon addWeeks(int weeks) {
        return add(Calendar.WEEK_OF_MONTH, weeks);
    }

    /**
     * Subtracts one week from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subWeek() {
        return subWeeks(1);
    }

    /**
     * Subtracts the given amount of weeks from the carbon instance.
     *
     * @param weeks the amount of weeks to subtract
     * @return the Carbon instance
     */
    public Carbon subWeeks(int weeks) {
        return sub(Calendar.WEEK_OF_MONTH, weeks);
    }

    /**
     * Adds one month to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addMonth() {
        return addMonths(1);
    }

    /**
     * Adds the given amount of months to the carbon instance.
     *
     * @param months the amount of months to add
     * @return the Carbon instance
     */
    public Carbon addMonths(int months) {
        return add(Calendar.MONTH, months);
    }

    /**
     * Subtracts one month from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subMonth() {
        return subMonths(1);
    }

    /**
     * Subtracts the given amount of months from the carbon instance.
     *
     * @param months the amount of months to subtract
     * @return the Carbon instance
     */
    public Carbon subMonths(int months) {
        return sub(Calendar.MONTH, months);
    }

    /**
     * Adds one year to the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon addYear() {
        return addYears(1);
    }

    /**
     * Adds the given amount of years to the carbon instance.
     *
     * @param years the amount of years to add
     * @return the Carbon instance
     */
    public Carbon addYears(int years) {
        return add(Calendar.YEAR, years);
    }

    /**
     * Subtracts one year from the carbon instance.
     *
     * @return the Carbon instance
     */
    public Carbon subYear() {
        return subYears(1);
    }

    /**
     * Subtracts the given amount of years from the carbon instance.
     *
     * @param years the amount of years to subtract
     * @return the Carbon instance
     */
    public Carbon subYears(int years) {
        return sub(Calendar.YEAR, years);
    }

    private int getPositive(int x) {
        if (x >= 0) {
            return x;
        }

        return x * -1;
    }

    private int getNegative(int x) {
        if (x < 0) {
            return x;
        }

        return x * -1;
    }

    /**
     * Compares the provided carbon object with the carbon instances, to see if they're equal.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the provided carbon instance matches
     * or (2) <code>FALSE</code> if they don't match.
     */
    public boolean eq(Carbon value) {
        return getTimestamp() == value.getTimestamp();
    }

    /**
     * Compares the provided carbon object with the carbon instances, to see if they're not equal.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the provided carbon instance matches
     * or (2) <code>FALSE</code> if they don't match.
     */
    public boolean ne(Carbon value) {
        return !eq(value);
    }

    ///////////////////////////////////////////////////////////////////
    /////////////////////////// COMPARISON ////////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Compares the provided carbon object with the carbon instances.
     * Checks if the current carbon instance is greater than the provided instance.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the current instance is greater than the provided instance
     * or (2) <code>FALSE</code> if it isn't greater than the provided instance.
     */
    public boolean gt(Carbon value) {
        return time.after(value.getTime());
    }

    /**
     * Compares the provided carbon object with the carbon instances.
     * Checks if the current carbon instance is greater than or equal to the provided instance.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the current instance is greater than or equal to the provided instance
     * or (2) <code>FALSE</code> if it isn't greater than or equal to the provided instance.
     */
    public boolean gte(Carbon value) {
        return gt(value) || eq(value);
    }

    /**
     * Compares the provided carbon object with the carbon instances.
     * Checks if the current carbon instance is less than the provided instance.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the current instance is less than the provided instance
     * or (2) <code>FALSE</code> if it isn't less than the provided instance.
     */
    public boolean lt(Carbon value) {
        return time.before(value.getTime());
    }

    /**
     * Compares the provided carbon object with the carbon instances.
     * Checks if the current carbon instance is less than or equal to the provided instance.
     *
     * @param value The carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the current instance is less than or equal to the provided instance
     * or (2) <code>FALSE</code> if it isn't less than or equal to the provided instance.
     */
    public boolean lte(Carbon value) {
        return lt(value) || eq(value);
    }

    /**
     * Compares the provided carbon objects with the carbon instances.
     * Checks if the current carbon instance is between the two provided carbon instances, or equal to either of them.
     *
     * @param first  The first carbon instance to compare with
     * @param second The second carbon instance to compare with
     * @return either (1) <code>TRUE</code> if the current instance is between the two provided instances
     * or (2) <code>FALSE</code> if the current instance isn't between the two provided instances.
     */
    public boolean between(Carbon first, Carbon second) {
        return between(first, second, true);
    }

    /**
     * Compares the provided carbon objects with the carbon instances.
     * Checks if the current carbon instance is between the two provided carbon instances.
     *
     * @param first      The first carbon instance to compare with
     * @param second     The second carbon instance to compare with
     * @param matchEqual Determines if a equal operator should be used as-well
     * @return either (1) <code>TRUE</code> if the current instance is between the two provided instances
     * or (2) <code>FALSE</code> if the current instance isn't between the two provided instances.
     */
    public boolean between(Carbon first, Carbon second, boolean matchEqual) {
        return matchEqual
            && (eq(first) || eq(second))
            || (time.before(first.getTime()) && time.after(second.getTime()))
            || (time.before(second.getTime()) && time.after(first.getTime()));

    }

    /**
     * Checks to see if the current date of the carbon instance matches yesterdays date.
     *
     * @return either (1) <code>TRUE</code> if the current date is the same as yesterdays date
     * or (2) <code>FALSE</code> if it doesn't match.
     */
    public boolean isYesterday() {
        Carbon carbon = new Carbon().subDay();

        return carbon.getYear() == getYear() && carbon.getMonth() == getMonth() && carbon.getDay() == getDay();
    }

    /**
     * Checks to see if the current date of the carbon instance matches todays date.
     *
     * @return either (1) <code>TRUE</code> if the current date is the same as todays date
     * or (2) <code>FALSE</code> if it doesn't match.
     */
    public boolean isToday() {
        Carbon carbon = new Carbon();

        return carbon.getYear() == getYear() && carbon.getMonth() == getMonth() && carbon.getDay() == getDay();
    }

    ///////////////////////////////////////////////////////////////////
    /////////////////////////// DIFFERENCES ///////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Checks to see if the current date of the carbon instance matches tomorrows date.
     *
     * @return either (1) <code>TRUE</code> if the current date is the same as tomorrows date
     * or (2) <code>FALSE</code> if it doesn't match.
     */
    public boolean isTomorrow() {
        Carbon carbon = new Carbon().addDay();

        return carbon.getYear() == getYear() && carbon.getMonth() == getMonth() && carbon.getDay() == getDay();
    }

    /**
     * Checks to see if the current date of the carbon instance is set in the past.
     *
     * @return either (1) <code>TRUE</code> if the current date is set to the past
     * or (2) <code>FALSE</code> if it's set to the present or future.
     */
    public boolean isPast() {
        return Calendar.getInstance(Locale.ENGLISH).getTimeInMillis() > time.getTimeInMillis();
    }

    /**
     * Checks to see if the current date of the carbon instance is set in the future.
     *
     * @return either (1) <code>TRUE</code> if the current date is set to the future
     * or (2) <code>FALSE</code> if it's set to the present or past.
     */
    public boolean isFuture() {
        return !isPast();
    }

    /**
     * Checks to see if the current day of the carbon instance is set on a weekday.
     *
     * @return either (1) <code>TRUE</code> if the current day is set to a weekday
     * or (2) <code>FALSE</code> if it's set to a weekend day.
     * @see Day#MONDAY
     * @see Day#TUESDAY
     * @see Day#WEDNESDAY
     * @see Day#THURSDAY
     * @see Day#FRIDAY
     */
    public boolean isWeekday() {
        return !isWeekend();
    }

    /**
     * Checks to see if the current day of the carbon instance is set on a weekday.
     *
     * @return either (1) <code>TRUE</code> if the current day is set to a weekday
     * or (2) <code>FALSE</code> if it's set to a weekend day.
     * @see Day#MONDAY
     * @see Day#TUESDAY
     * @see Day#WEDNESDAY
     * @see Day#THURSDAY
     * @see Day#FRIDAY
     */
    public boolean isWeekend() {
        int currentDay = getDay();

        return WEEKEND_DAYS.stream().anyMatch((day) -> (day.getId() == currentDay));
    }

    /**
     * Checks to see if the current year is a leap year.
     *
     * @return <code>TRUE</code> if the year is a leap year, <code>FALSE</code> otherwise.
     */
    public boolean isLeapYear() {
        return time.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }

    /**
     * Checks to see if the current day matches the provided carbon instances day.
     *
     * @param other The carbon instance to compare with
     * @return <code>TRUE</code> if the day matches, <code>FALSE</code> otherwise.
     */
    public boolean isSameDay(Carbon other) {
        return getYear() == other.getYear() && getMonth() == other.getMonth() && getDay() == other.getDay();
    }

    /**
     * Checks to see if the current day and month matches the provided carbon instances day and month.
     *
     * @param other The carbon instance to compare with
     * @return <code>TRUE</code> if the day and month matches, <code>FALSE</code> otherwise.
     */
    public boolean isBirthday(Carbon other) {
        return getMonth() == other.getMonth() && getDay() == other.getDay();
    }

    /**
     * Gets the difference between now and the carbon instance in seconds.
     *
     * @return the difference between now and the carbon instance in seconds
     */
    public long diffInSeconds() {
        long current = System.currentTimeMillis();
        long unixTime = time.getTimeInMillis();

        long value = (current - unixTime) / 1000;

        return value >= 0 ? value : value * -1;
    }

    /**
     * Gets the difference between the provided carbon instance and the carbon instance in seconds.
     *
     * @param other The carbon instance to compare with
     * @return the difference between now and the carbon instance in seconds
     */
    public long diffInSeconds(Carbon other) {
        long current = other.getTime().getTimeInMillis();
        long unixTime = time.getTimeInMillis();

        long value = (current - unixTime) / 1000;

        return value >= 0 ? value : value * -1;
    }

    /**
     * Gets the difference between now and the carbon instance in minutes.
     *
     * @return the difference between now and the carbon instance in minutes.
     */
    public long diffInMinutes() {
        return diffInSeconds() / Time.SECONDS_PER_MINUTE.getTime();
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in minutes.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in minutes.
     */
    public long diffInMinutes(Carbon other) {
        return diffInSeconds(other) / Time.SECONDS_PER_MINUTE.getTime();
    }

    /**
     * Gets the difference between now and the carbon instance in hours.
     *
     * @return the difference between now and the carbon instance in hours.
     */
    public long diffInHours() {
        return diffInMinutes() / Time.MINUTES_PER_HOUR.getTime();
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in hours.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in hours.
     */
    public long diffInHours(Carbon other) {
        return diffInMinutes(other) / Time.MINUTES_PER_HOUR.getTime();
    }

    /**
     * Gets the difference between now and the carbon instance in days.
     *
     * @return the difference between now and the carbon instance in days.
     */
    public long diffInDays() {
        return diffInHours() / Time.HOURS_PER_DAY.getTime();
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in days.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in days.
     */
    public long diffInDays(Carbon other) {
        return diffInHours(other) / Time.HOURS_PER_DAY.getTime();
    }

    /**
     * Gets the difference between now and the carbon instance in weeks.
     *
     * @return the difference between now and the carbon instance in weeks.
     */
    public long diffInWeeks() {
        return diffInDays() / Time.DAYS_PER_WEEK.getTime();
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in weeks.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in weeks.
     */
    public long diffInWeeks(Carbon other) {
        return diffInDays(other) / Time.DAYS_PER_WEEK.getTime();
    }

    /**
     * Gets the difference between now and the carbon instance in months.
     *
     * @return the difference between now and the carbon instance in months.
     */
    public long diffInMonths() {
        Carbon now = new Carbon();

        return getPositive((getYear() - now.getYear()) * 12 + time.get(Calendar.MONTH) - now.getTime().get(Calendar.MONTH));
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in months.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in months.
     */
    public long diffInMonths(Carbon other) {
        return getPositive((getYear() - other.getYear()) * 12 + time.get(Calendar.MONTH) - other.getTime().get(Calendar.MONTH));
    }

    /**
     * Gets the difference between now and the carbon instance in years.
     *
     * @return the difference between now and the carbon instance in years.
     */
    public long diffInYears() {
        Carbon now = new Carbon();

        return getPositive(now.getYear() - getYear());
    }

    /**
     * Gets the difference between the provided carbon instance and the current carbon instance in years.
     *
     * @param other The carbon instance to compare with.
     * @return the difference between now and the carbon instance in years.
     */
    public long diffInYears(Carbon other) {
        return getPositive(other.getYear() - getYear());
    }

    /**
     * Get the difference between now and the carbon instance time in a human readable string.
     *
     * @return the difference between now and the carbon instance in a human readable string
     */
    public String diffForHumans() {
        return diffForHumans(false);
    }

    /**
     * Get the difference between now and the carbon instance time in a human readable string.
     *
     * @param removeModifiers Determines if the modifiers "from now" and "ago" should be removed
     * @return the difference between now and the carbon instance in a human readable string
     */
    public String diffForHumans(boolean removeModifiers) {
        long unix = diffInSeconds();

        if (unix == 0) {
            return "now";
        }

        StringBuilder builder = parseDiffForHumans(unix);

        if (!removeModifiers) {
            if (isPast()) {
                builder.append(" ago");
            } else {
                builder.append(" from now");
            }
        }

        return builder.toString().trim();
    }

    /**
     * Get the difference between the provided Carbon instance and the carbon instance time in a human readable string.
     *
     * @param other The carbon instance to compare with
     * @return the difference between the provided Carbon instance and the carbon instance in a human readable string
     */
    public String diffForHumans(Carbon other) {
        return diffForHumans(other, false);
    }

    /**
     * Get the difference between the provided Carbon instance and the carbon instance time in a human readable string.
     *
     * @param other           The carbon instance to compare with
     * @param removeModifiers Determines if the modifiers "from now" and "ago" should be removed
     * @return the difference between the provided Carbon instance and the carbon instance in a human readable string
     */
    public String diffForHumans(Carbon other, boolean removeModifiers) {
        long value = getTimestamp() - other.getTimestamp();

        long unix = value >= 0 ? value : value * -1;

        if (unix == 0) {
            return "now";
        }

        StringBuilder builder = parseDiffForHumans(unix);

        if (!removeModifiers) {
            if (other.time.getTimeInMillis() > time.getTimeInMillis()) {
                builder.append(" after");
            } else {
                builder.append(" before");
            }
        }

        return builder.toString().trim();
    }

    private StringBuilder parseDiffForHumans(long unix) {
        StringBuilder sb = new StringBuilder();

        long sec = (unix >= 60 ? unix % 60 : unix);
        long min = (unix = (unix / 60)) >= 60 ? unix % 60 : unix;
        long hrs = (unix = (unix / 60)) >= 24 ? unix % 24 : unix;
        long days = (unix = (unix / 24)) >= 30 ? unix % 30 : unix;
        long months = (unix = (unix / 30)) >= 12 ? unix % 12 : unix;
        long years = (unix / 12);

        if (years > 0) {
            if (years == 1) {
                sb.append("a year");
            } else {
                sb.append(years).append(" years");
            }

            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" and a month");
                } else {
                    sb.append(" and ").append(months).append(" months");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("a month");
            } else {
                sb.append(months).append(" months");
            }

            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" and a day");
                } else {
                    sb.append(" and ").append(days).append(" days");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("a day");
            } else {
                sb.append(days).append(" days");
            }

            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" and an hour");
                } else {
                    sb.append(" and ").append(hrs).append(" hours");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("an hour");
            } else {
                sb.append(hrs).append(" hours");
            }

            if (min > 1) {
                sb.append(" and ").append(min).append(" minutes");
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append("a minute");
            } else {
                sb.append(min).append(" minutes");
            }

            if (sec > 1) {
                sb.append(" and ").append(sec).append(" seconds");
            }
        } else if (sec <= 1) {
            sb.append("about a second");
        } else {
            sb.append(sec).append(" seconds");
        }

        return sb;
    }

    /**
     * Sets the carbon time to the start of the day.
     *
     * @return the Carbon instance
     */
    public Carbon startOfDay() {
        return setHour(0).setMinute(0).setSecond(0);
    }

    /**
     * Sets the carbon time to the end of the day.
     *
     * @return the Carbon instance
     */
    public Carbon endOfDay() {
        return setHour(23).setMinute(59).setSecond(59);
    }

    ///////////////////////////////////////////////////////////////////
    //////////////////////////// MODIFIERS ////////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Sets the carbon time to the start of the week.
     *
     * @return the Carbon instance
     */
    public Carbon startOfWeek() {
        return setDayOfWeek(WEEK_START_AT.getId()).startOfDay();
    }

    /**
     * Sets the carbon time to the end of the week.
     *
     * @return the Carbon instance
     */
    public Carbon endOfWeek() {
        return setDay(WEEK_END_AT.getId()).endOfDay();
    }

    /**
     * Sets the carbon time to the start of the month.
     *
     * @return the Carbon instance
     */
    public Carbon startOfMonth() {
        return startOfDay().setDay(1);
    }

    /**
     * Sets the carbon time to the end of the month.
     *
     * @return the Carbon instance
     */
    public Carbon endOfMonth() {
        Calendar cal = new GregorianCalendar(getYear(), getMonth(), getDay());

        return setDay(cal.getActualMaximum(Calendar.DAY_OF_MONTH)).endOfDay();
    }

    /**
     * Sets the carbon time to the start of the year.
     *
     * @return the Carbon instance
     */
    public Carbon startOfYear() {
        return setMonth(1).startOfMonth();
    }

    /**
     * Sets the carbon time to the end of the year.
     *
     * @return the Carbon instance
     */
    public Carbon endOfYear() {
        return setMonth(Time.MONTHS_PER_YEAR.getTime()).endOfMonth();
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////////// OUTPUT FORMAT //////////////////////////
    ///////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return format(toStringFormat);
    }

    /**
     * Generates a date string, example:
     * <p>
     * 1975-12-25
     *
     * @return The generated date string
     */
    public String toDateString() {
        return format(Formats.DATE);
    }

    /**
     * Generates a formatted date string, example:
     * <p>
     * Dec 25, 1975
     *
     * @return The generated formatted date string
     */
    public String toFormattedDateString() {
        return format(Formats.FORMATTED_DATE);
    }

    /**
     * Generates a time string, example:
     * <p>
     * 14:15:16
     *
     * @return The generated time string
     */
    public String toTimeString() {
        return format(Formats.TIME);
    }

    /**
     * Generates a time offset time string, example:
     * <p>
     * 14:15:16-05:00
     *
     * @return The generated time offset time string
     */
    public String toTimeOffsetString() {
        return format(Formats.TIME_OFFSET);
    }

    /**
     * Generates a date time string, example:
     * <p>
     * 1975-12-25 14:15:16
     *
     * @return The generated date time string
     */
    public String toDateTimeString() {
        return format(Formats.DATE_TIME);
    }

    /**
     * Generates a day date time string, example:
     * <p>
     * Thu, Dec 25, 1975 2:15 PM
     *
     * @return The generated day date time string
     */
    public String toDayDateTimeString() {
        return format(Formats.DAY_DATE_TIME);
    }

    /**
     * Generates an atomic time string, example:
     * <p>
     * 1975-12-25T14:15:16-05:00
     *
     * @return The generated atomic time string
     */
    public String toAtomicString() {
        return String.format("%sT%s", toDateString(), toTimeOffsetString());
    }

    /**
     * Generates a cookie time string, example:
     * <p>
     * Thursday, 25-Dec-1975 14:15:16 EST
     *
     * @return The generated cookie time string
     */
    public String toCookieString() {
        return format(Formats.COOKIE);
    }

    /**
     * Generates a ISO 8601 time string, example:
     * <p>
     * 1975-12-25T14:15:16-0500
     *
     * @return The generated ISO 8601 time string
     */
    public String toIso8601String() {
        return String.format("%sT%s", toDateString(), format("HH:mm:ssZ"));
    }

    /**
     * Generates a RFC 822 time string, example:
     * <p>
     * Thu, 25 Dec 1975 14:15:16 -0500
     *
     * @return The generated RFC 822 time string
     */
    public String toRfc822String() {
        return format(Formats.RFC_822);
    }

    /**
     * Generates a RFC 850 time string, example:
     * <p>
     * Thursday, 25-Dec-1975 14:15:16 EST
     *
     * @return The generated RFC 850 time string
     */
    public String toRfc850String() {
        return format(Formats.RFC_850);
    }

    /**
     * Generates a RFC 1036 time string, example:
     * <p>
     * 1975-12-25T14:15:16-05:00
     *
     * @return The generated RFC 1036 time string
     */
    public String toRfc1036String() {
        return format(Formats.RFC_1036);
    }

    /**
     * Generates a RFC 850 time string, example:
     * <p>
     * Thu, 25 Dec 1975 14:15:16 -0500
     *
     * @return The generated RFC 850 time string
     */
    public String toRfc3339String() {
        return String.format("%sT%s", toDateString(), format("HH:mm:ssZ"));
    }

    /**
     * Generates a RSS time string, example:
     * <p>
     * Thu, 25 Dec 1975 14:15:16 -0500
     *
     * @return The generated RSS time string
     */
    public String toRssString() {
        return format(Formats.RSS);
    }

    /**
     * Generates a W3C time string, example:
     * <p>
     * 1975-12-25T14:15:16-05:00
     *
     * @return The generated W3C time string
     */
    public String toW3cString() {
        return String.format("%sT%s", format("yyyy-mm-dd"), toTimeOffsetString());
    }

    /**
     * Formats the carbon instance and prints out the formatted time string.
     *
     * @param format the string to use to generate the time string
     * @return the formatted datetime string
     */
    public String format(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);

        if (timezone != null) {
            sdf.setTimeZone(timezone);
        }

        return sdf.format(time.getTime());
    }

    /**
     * Formats the carbon instance and prints out the formatted time string.
     *
     * @param format the string to use to generate the time string
     * @return the formatted datetime string
     */
    public String format(Formats format) {
        return format(format.getFormat());
    }

    /**
     * Creates a copy of the current Carbon instance.
     *
     * @return a copy of the current Carbon instance
     * @see #Carbon(com.avairebot.time.Carbon)
     */
    public Carbon copy() {
        return new Carbon(this);
    }
}
