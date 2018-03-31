package com.avairebot.utilities;

import java.util.function.Consumer;

public class RestActionUtil {

    /**
     * This function does nothing other than work as a rest
     * action failure consumer that ignores the failure.
     */
    public static final Consumer<Throwable> IGNORE = ignored -> {
        // Nothing to see here
    };
}
