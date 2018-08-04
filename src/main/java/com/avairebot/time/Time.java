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
