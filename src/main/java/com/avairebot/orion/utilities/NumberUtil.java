package com.avairebot.orion.utilities;

public class NumberUtil {

    /**
     * Parses the string argument as a signed integer, if the string argument
     * is not a valid integer 0 will be returned as the default instead.
     *
     * @param string The string integer that should be parsed.
     * @return The integer represented by the string argument.
     */
    public static int parseInt(String string) {
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
    public static int parseInt(String string, int def) {
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
    public static int parseInt(String string, int def, int radix) {
        try {
            return Integer.parseInt(string, radix);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    /**
     * Parses the given number, making sure the number is greater than
     * the minimum number given, and less than the max number given.
     *
     * @param number The number that should be parsed.
     * @param min    The max value the number can be.
     * @param max    The minimum value the number can be.
     * @return
     */
    public static int getBetween(int number, int min, int max) {
        return Math.max(min, Math.min(max, number));
    }
}
