package com.avairebot.utilities;

import java.util.Arrays;
import java.util.HashSet;

public class ComparatorUtil {

    private final static HashSet<String> fuzzyTrue = new HashSet<>(Arrays.asList("yes", "y", "on", "enable", "true", "confirm", "1"));
    private final static HashSet<String> fuzzyFalse = new HashSet<>(Arrays.asList("no", "n", "off", "disable", "false", "0"));

    /**
     * Checks if the given {@code text} matches a true statement using the {@link #fuzzyTrue} hash set.
     *
     * @param string The string that should be checked.
     * @return True if the given text can be considered true.
     */
    public static boolean isFuzzyTrue(String string) {
        return string != null && fuzzyTrue.contains(string.toLowerCase());
    }

    /**
     * Checks if the given {@code text} matches a false statement using the {@link #fuzzyFalse} hash set.
     *
     * @param string The string that should be checked.
     * @return True if the given text can be considered false.
     */
    public static boolean isFuzzyFalse(String string) {
        return string != null && fuzzyFalse.contains(string.toLowerCase());
    }

    /**
     * Gets the fuzzy comparison value from the given string, if the string doesn't contain
     * any valid fuzzy types, {@link ComparatorType#UNKNOWN} will be returned.
     *
     * @param string The string that should be checked.
     * @return The matching companions value matching the given string, or {@link ComparatorType#UNKNOWN} if there were no match.
     */
    public static ComparatorType getFuzzyType(String string) {
        if (isFuzzyTrue(string)) {
            return ComparatorType.TRUE;
        }

        if (isFuzzyFalse(string)) {
            return ComparatorType.FALSE;
        }

        return ComparatorType.UNKNOWN;
    }

    public enum ComparatorType {
        TRUE(true), FALSE(false), UNKNOWN(false);

        private final boolean value;

        ComparatorType(boolean type) {
            this.value = type;
        }

        public boolean getValue() {
            return value;
        }
    }
}
