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

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {

    private static final Pattern timestampPattern = Pattern.compile("^(\\d?\\d)(?::([0-5]?\\d))?(?::([0-5]?\\d))?$");
    private static final Pattern numberPattern = Pattern.compile("[-+]?\\d*\\.?\\d+");
    private static final DecimalFormat niceFormat = new DecimalFormat("#,##0");
    private static final DecimalFormat niceFormatWithDecimal = new DecimalFormat("#,###.##");

    /**
     * Parses the string argument as a signed integer, if the string argument
     * is not a valid integer 0 will be returned as the default instead.
     *
     * @param string The string integer that should be parsed.
     * @return The integer represented by the string argument.
     */
    public static int parseInt(@Nonnull String string) {
        return parseInt(string, 0);
    }

    /**
     * Parses the string argument as a signed integer, if the string argument
     * is not a valid integer, the default will be returned instead.
     *
     * @param string The string integer that should be parsed.
     * @param def    The default integer if the string argument is not a valid integer.
     * @return The integer represented by the string argument.
     */
    public static int parseInt(@Nonnull String string, int def) {
        return parseInt(string, def, 10);
    }

    /**
     * Parses the string argument as a signed integer, if the string argument
     * is not a valid integer, the default will be returned instead, the
     * integer will be parsed using the given radix.
     *
     * @param string The string integer that should be parsed.
     * @param def    The default integer if the string argument is not a valid integer.
     * @param radix  the radix to be used while parsing {@code string}.
     * @return The integer represented by the string argument.
     */
    public static int parseInt(@Nonnull String string, int def, int radix) {
        try {
            return Integer.parseInt(string, radix);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    /**
     * Parses the given string to it's value in milliseconds
     * if it matches the {@link #timestampPattern} pattern.
     *
     * @param string The string that should be parsed to its milliseconds value.
     * @return The parsed number value in milliseconds matching the given string.
     * @throws IllegalStateException If a string is given that does not match the {@link #timestampPattern} pattern this exception is thrown.
     */
    public static long parseTimeString(@Nonnull String string) {
        long seconds = 0;
        long minutes = 0;
        long hours = 0;

        Matcher matcher = timestampPattern.matcher(string);

        matcher.find();

        int capturedGroups = 0;
        if (matcher.group(1) != null) capturedGroups++;
        if (matcher.group(2) != null) capturedGroups++;
        if (matcher.group(3) != null) capturedGroups++;

        switch (capturedGroups) {
            case 0:
                throw new IllegalStateException("Unable to match " + string);
            case 1:
                seconds = NumberUtil.parseInt(matcher.group(1));
                break;
            case 2:
                minutes = NumberUtil.parseInt(matcher.group(1));
                seconds = NumberUtil.parseInt(matcher.group(2));
                break;
            case 3:
                hours = NumberUtil.parseInt(matcher.group(1));
                minutes = NumberUtil.parseInt(matcher.group(2));
                seconds = NumberUtil.parseInt(matcher.group(3));
                break;
        }

        minutes = minutes + hours * 60;
        seconds = seconds + minutes * 60;

        return seconds * 1000;
    }

    /**
     * Parses the given number, making sure the number is greater than
     * the minimum number given, and less than the max number given.
     *
     * @param number The number that should be parsed.
     * @param min    The max value the number can be.
     * @param max    The minimum value the number can be.
     * @return Gets the number that is greater that the minimum and less than the maximum.
     */
    public static int getBetween(int number, int min, int max) {
        return Math.min(max, Math.max(min, number));
    }

    /**
     * Formats time in milliseconds into it's "time" format, into hours:minutes:seconds, if
     * the given {@code millis} is {@link Long#MAX_VALUE} "live" will be returned instead.
     *
     * @param millis The amount of time in milliseconds that should be formatted.
     * @return The formatted timestring.
     */
    public static String formatTime(long millis) {
        if (millis == Long.MAX_VALUE) {
            return "LIVE";
        }

        long t = millis / 1000L;
        int sec = (int) (t % 60L);
        int min = (int) ((t % 3600L) / 60L);
        int hrs = (int) (t / 3600L);

        if (hrs != 0) {
            return forceTwoDigits(hrs) + ":" + forceTwoDigits(min) + ":" + forceTwoDigits(sec);
        }
        return forceTwoDigits(min) + ":" + forceTwoDigits(sec);
    }

    /**
     * Force the given integer to two digits by adding a 0
     * to the front of the number if it is less than 10.
     *
     * @param integer The integer that should be formatted.
     * @return The formatted integer in its string form.
     */
    public static String forceTwoDigits(int integer) {
        return integer < 10 ? "0" + integer : Integer.toString(integer);
    }

    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242352</code> will get turned into <code>9,242,352</code>
     *
     * @param value The long value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicely(long value) {
        return niceFormat.format(value);
    }


    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242352</code> will get turned into <code>9,242,352</code>
     *
     * @param value The float value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicely(float value) {
        return niceFormat.format(value);
    }

    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242352</code> will get turned into <code>9,242,352</code>
     *
     * @param value The double value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicely(double value) {
        return niceFormat.format(value);
    }

    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242352</code> will get turned into <code>9,242,352</code>
     *
     * @param value The integer value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicely(int value) {
        return niceFormat.format(value);
    }


    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242.352</code> will get turned into <code>9,242.35</code>
     *
     * @param value The float value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicelyWithDecimals(float value) {
        return niceFormatWithDecimal.format(value);
    }

    /**
     * Formats the number into a more human-readable format by adding commas to indicate thousands.
     * <p>
     * Example: <code>9242352</code> will get turned into <code>9,242.35</code>
     *
     * @param value The double value that should be formatted.
     * @return The formatted value.
     */
    public static String formatNicelyWithDecimals(double value) {
        return niceFormatWithDecimal.format(value);
    }

    /**
     * Checks if the given string is a numeric string, only containing numbers.
     *
     * @param string The string that should be checked if it is numeric.
     * @return True if the string is numeric, false otherwise.
     */
    public static boolean isNumeric(@Nonnull String string) {
        return numberPattern.matcher(string).matches();
    }
}
