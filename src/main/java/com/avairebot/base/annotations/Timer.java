/*
 * InitPlugin.java
 *
 * Copyright (c) 2007, Ralf Biedert All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.avairebot.base.annotations;

import java.lang.Thread;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with &#064;Timer will called periodically from a timer. The method
 * may return a boolean value. If this value is true, the timer will be canceled. For 
 * example, to specify that after the plugin's creation a specific method should be 
 * called from a timer, you would write:<br/><br/>
 * 
 * <code>
 * &#064;Timer<br/>
 * public void ping() { ... }
 * </code><br/><br/>
 * 
 * All timers are terminated upon <code>BackendPluginManager.shutdown()</code>.

 * @author Ralf Biedert
 * @see Thread
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timer {
    /**
     * Type of the timer.
     *
     * @author Ralf Biedert
     *
     */
    public static enum TimerType {
        /**
         * Delay based timer
         */
        DELAY_BASED,
        /**
         * Rate based timer
         */
        RATE_BASED
    }

    /**
     * Period of the timer.
     *
     * @return .
     */
    long period();

    /**
     * When to start the timer.
     *
     * @return .
     */
    long startupDelay() default 0;

    /**
     * Specifies the type of this timer.
     *
     * @return .
     */
    TimerType timerType() default TimerType.DELAY_BASED;
}
