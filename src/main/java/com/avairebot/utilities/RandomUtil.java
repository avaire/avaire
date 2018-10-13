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
import java.security.SecureRandom;
import java.util.List;

public class RandomUtil {

    /**
     * The globally used random instance that
     * is used for all randomized... things.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Returns true or false randomly.
     *
     * @return Either true or false.
     */
    public static boolean getBoolean() {
        return RANDOM.nextBoolean();
    }

    /**
     * Get a random integer within the bounds given, so the
     * integer returned will be a random integer between
     * 0 and the given bound integer - 1.
     *
     * @param bound The max bound for the random integer.
     * @return The random integer.
     */
    public static int getInteger(int bound) {
        if (bound <= 0) {
            return 0;
        }
        return RANDOM.nextInt(bound);
    }

    /**
     * Pick one random string from the given list of strings.
     *
     * @param strings The strings that should be randomized.
     * @return The random string picked from the list of given strings.
     */
    public static String pickRandom(@Nonnull String... strings) {
        return strings[RANDOM.nextInt(strings.length)];
    }

    /**
     * Pick one random string from the given list of strings.
     *
     * @param strings The list of strings that should be used to pick a random string.
     * @return The random string picked from the list of given strings.
     */
    public static Object pickRandom(@Nonnull List<?> strings) {
        return strings.get(RANDOM.nextInt(strings.size()));
    }
}
