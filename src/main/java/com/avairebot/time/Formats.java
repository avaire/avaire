package com.avairebot.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum Formats {

    /**
     * Generates a date time string, example:
     * <p>
     * 1975-12-25 14:15:16
     */
    DATE_TIME("yyyy-MM-dd HH:mm:ss"),
    /**
     * Generates a date string, example:
     * <p>
     * 1975-12-25
     */
    DATE("yyyy-MM-dd"),
    /**
     * Generates a formatted date string, example:
     * <p>
     * Dec 25, 1975
     */
    FORMATTED_DATE("MMMMM dd, yyyy"),
    /**
     * Generates a time string, example:
     * <p>
     * 14:15:16
     */
    TIME("HH:mm:ss"),
    /**
     * Generates a time offset time string, example:
     * <p>
     * 14:15:16-05:00
     */
    TIME_OFFSET("HH:mm:ssXXX"),
    /**
     * Generates a day date time string, example:
     * <p>
     * Thu, Dec 25, 1975 2:15 PM
     */
    DAY_DATE_TIME("EEE, MMM dd, yyyy h:mm aaa"),
    /**
     * Generates a cookie time string, example:
     * <p>
     * Thursday, 25-Dec-1975 14:15:16 EST
     */
    COOKIE("EEEEEEEE, dd-MMM-yyyy HH:mm:ss z"),
    /**
     * Generates a RFC 822 time string, example:
     * <p>
     * Thu, 25 Dec 1975 14:15:16 -0500
     */
    RFC_822("EEE, dd MMM yyyy HH:mm:ss Z"),
    /**
     * Generates a RFC 850 time string, example:
     * <p>
     * Thursday, 25-Dec-1975 14:15:16 EST
     */
    RFC_850("EEEEEEEE, dd-MMM-yyyy HH:mm:ss z"),
    /**
     * Generates a RFC 1036 time string, example:
     * <p>
     * 1975-12-25T14:15:16-05:00
     */
    RFC_1036("EEE, dd MMM yyyy HH:mm:ssXXX"),
    /**
     * Generates a RSS time string, example:
     * <p>
     * Thu, 25 Dec 1975 14:15:16 -0500
     */
    RSS("EEE, dd MMM yyyy HH:mm:ss Z");

    private final String string;

    Formats(String string) {
        this.string = string;
    }

    /**
     * Gets the {@link java.text.SimpleDateFormat} format as a string.
     *
     * @return the {@link java.text.SimpleDateFormat} format as a string.
     */
    public String getFormat() {
        return string;
    }

    /**
     * Creates a {@link java.text.SimpleDateFormat} object from the format string.
     *
     * @return the {@link java.text.SimpleDateFormat} format what was created.
     */
    public SimpleDateFormat make() {
        return new SimpleDateFormat(string);
    }

    /**
     * Attempts to parse the provided time string with the format.
     *
     * @param time The time string to try and parse
     * @return the created {@link java.util.Date} object from the time string.
     * @throws ParseException if the provided time doesn't match the provided format.
     */
    public Date parse(String time) throws ParseException {
        return make().parse(time);
    }

    @Override
    public String toString() {
        return string;
    }
}
