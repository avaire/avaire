package com.avairebot.contracts.middleware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrottleMessage {

    /**
     * Determines if the set message should overwrite the default throttle message
     * in the event the command has been throttled, if set to false the message
     * will just be appended to the throttle message instead.
     *
     * @return <code>True</code> if the message should overwrite the throttle message, or
     * <code>False</code> if the message should be appended to the default throttle message instead.
     */
    boolean overwrite() default true;

    /**
     * The throttle message that should be appended of overwrite the default throttle message.
     *
     * @return The throttle message.
     */
    String message() default "";
}
