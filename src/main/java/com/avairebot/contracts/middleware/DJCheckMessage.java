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

