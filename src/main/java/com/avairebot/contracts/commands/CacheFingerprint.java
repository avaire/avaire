package com.avairebot.contracts.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheFingerprint {

    /**
     * The cache fingerprint that should be used for commands, setting a custom
     * cache fingerprint for a command can allow different commands to share
     * the same limits for things like command throttling.
     *
     * @return The cache fingerprint.
     */
    String name() default "";
}
