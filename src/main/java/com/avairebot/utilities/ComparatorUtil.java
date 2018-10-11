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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class ComparatorUtil {

    private final static HashSet<String> fuzzyTrue = new HashSet<>(Arrays.asList("yes", "y", "on", "enable", "true", "confirm", "1"));
    private final static HashSet<String> fuzzyFalse = new HashSet<>(Arrays.asList("no", "n", "off", "disable", "false", "reset", "0"));

    /**
     * Checks if the given {@code text} matches a true statement using the {@link #fuzzyTrue} hash set.
     *
     * @param string The string that should be checked.
     * @return True if the given text can be considered true.
     */
    public static boolean isFuzzyTrue(@Nullable String string) {
        return string != null && fuzzyTrue.contains(string.toLowerCase());
    }

    /**
     * Checks if the given {@code text} matches a false statement using the {@link #fuzzyFalse} hash set.
     *
     * @param string The string that should be checked.
     * @return True if the given text can be considered false.
     */
    public static boolean isFuzzyFalse(@Nullable String string) {
        return string != null && fuzzyFalse.contains(string.toLowerCase());
    }

    /**
     * Gets the fuzzy comparison value from the given string, if the string doesn't contain
     * any valid fuzzy types, {@link ComparatorType#UNKNOWN} will be returned.
     *
     * @param string The string that should be checked.
     * @return The matching companions value matching the given string, or {@link ComparatorType#UNKNOWN} if there were no match.
     */
    public static ComparatorType getFuzzyType(@Nullable String string) {
        if (isFuzzyTrue(string)) {
            return ComparatorType.TRUE;
        }

        if (isFuzzyFalse(string)) {
            return ComparatorType.FALSE;
        }

        return ComparatorType.UNKNOWN;
    }

    /**
     * The comparator types, they are used to represent
     * the actually values of the comparator.
     */
    public enum ComparatorType {

        /**
         * Represents the true value for the comparison.
         */
        TRUE(true),

        /**
         * Represents the false value for the comparison.
         */
        FALSE(false),

        /**
         * Represents an unknown value for the comparison.
         */
        UNKNOWN(false);

        private final boolean value;

        ComparatorType(boolean type) {
            this.value = type;
        }

        /**
         * Gets the value for the comparison result.
         *
         * @return The value for the comparison result.
         */
        public boolean getValue() {
            return value;
        }
    }
}
