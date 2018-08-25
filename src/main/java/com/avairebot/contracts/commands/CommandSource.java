package com.avairebot.contracts.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandSource {

    /**
     * The URL to find the source code for the given command, this is used by
     * the "Source Command" for finding the source code for commands on
     * github, commands that belongs to the core will automatically be
     * assigned this scope, while plugins registered from a plugin
     * will have to add the scope themselves if they wish to make
     * their command work with the source command.
     *
     * @return The URL to where other people can find the source code for the command.
     */
    String uri() default "";
}
