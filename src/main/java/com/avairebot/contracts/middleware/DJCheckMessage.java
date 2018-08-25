package com.avairebot.contracts.middleware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DJCheckMessage {

    /**
     * Determines if the set message should overwrite the default DJ check message in the
     * event the command was used by someone with an insufficient DJ level, if set to
     * false the message will just be appended to the DJ check message instead.
     *
     * @return <code>True</code> if the message should overwrite the throttle message, or
     * <code>False</code> if the message should be appended to the default throttle message instead.
     */
    boolean overwrite() default true;

    /**
     * The DJ check message that should be appended of overwrite the default DJ check message.
     *
     * @return The DJ check message.
     */
    String message() default "";
}

