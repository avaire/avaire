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
import java.util.Arrays;
import java.util.List;

public class RandomUtil {

    /**
     * A list of characters that can be used to generate the string randomly.
     */
    private static final List<String> characterSet = Arrays.asList(
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m",
        "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M",
        "0", "1", "2", "3", "4", "5", "6", "7", "9", "8", "!", "%", "&", "(", ")", "[", "]", "{", "}"
    );

    /**
     * The globally used random instance that
     * is used for all randomized... things.
     */
    private static final SecureRandom random = new SecureRandom();

    /**
     * Returns true or false randomly.
     *
     * @return Either true or false.
     */
    public static boolean getBoolean() {
        return random.nextBoolean();
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
        return random.nextInt(bound);
    }

    /**
     * Pick one random string from the given list of strings.
     *
     * @param strings The strings that should be randomized.
     * @return The random string picked from the list of given strings.
     */
    public static String pickRandom(@Nonnull String... strings) {
        return strings[random.nextInt(strings.length)];
    }

    /**
     * Pick one random string from the given list of strings.
     *
     * @param strings The list of strings that should be used to pick a random string.
     * @return The random string picked from the list of given strings.
     */
    public static Object pickRandom(@Nonnull List<?> strings) {
        return strings.get(random.nextInt(strings.size()));
    }

    /**
     * Generate a randomly generated string with the given length.
     *
     * @param length The length of the randomly generated string.
     * @return The randomly generated string.
     */
    public static String generateString(int length) {
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            tokenBuilder.append(RandomUtil.pickRandom(characterSet));
        }
        return tokenBuilder.toString();
    }
}
