package com.avairebot.utilities;

import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.requests.ErrorResponse;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.function.Consumer;

public class RestActionUtil {

    /**
     * This function does nothing other than work as a rest
     * action failure consumer that ignores the failure.
     */
    public static final Consumer<Throwable> ignore = ignored -> {
        // Nothing to see here
    };

    /**
     * This functions passes on the error to the default failure,
     * unless it meets some special caterina, in which case it
     * may either be ignored, or handled differently.
     */
    public static final Consumer<Throwable> handleMessageCreate = error -> {
        if (error == null) return;

        if (error instanceof ErrorResponseException) {
            if (((ErrorResponseException) error).getErrorCode() == ErrorResponse.MISSING_ACCESS.getCode()) {
                return; // Ignore missing access errors
            }
        }

        RestAction.DEFAULT_FAILURE.accept(error);
    };
}
