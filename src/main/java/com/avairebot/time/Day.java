package com.avairebot.time;

import java.util.Calendar;

public enum Day {

    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Monday.
     */
    MONDAY("Monday", Calendar.MONDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Tuesday.
     */
    TUESDAY("Tuesday", Calendar.TUESDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Wednesday.
     */
    WEDNESDAY("Wednesday", Calendar.WEDNESDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Thursday.
     */
    THURSDAY("Thursday", Calendar.THURSDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Friday.
     */
    FRIDAY("Friday", Calendar.FRIDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Saturday.
     */
    SATURDAY("Saturday", Calendar.SATURDAY),
    /**
     * Value of the {@link java.util.Calendar#DAY_OF_WEEK} field indicating
     * Sunday.
     */
    SUNDAY("Sunday", Calendar.SUNDAY);

    private final String name;
    private final int id;

    private Day(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Gets the day from the id, if an invalid id was
     * given, <code>NULL</code> will returned instead.
     *
     * @param id the id to match with the day
     * @return the day that match the provided id
     */
    public static Day fromId(int id) {
        for (Day day : values()) {
            if (day.getId() == id) {
                return day;
            }
        }

        return null;
    }

    /**
     * Gets the day name.
     *
     * @return the day name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the day ID.
     *
     * @return the day id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the day before the current day.
     *
     * @return the day before the current day
     */
    public Day getYesterday() {
        int day = getId() - 1;

        return day <= 0 ? SATURDAY : fromId(day);
    }

    /**
     * Gets the day after the current day.
     *
     * @return the day after the current day
     */
    public Day getTomorrow() {
        int day = getId() + 1;

        return day > 7 ? SUNDAY : fromId(day);
    }
}
