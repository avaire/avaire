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
package com.avairebot.base.annotations.events;

import java.lang.Shutdown;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.annotations.Timer;
import com.avairebot.base.annotations.injections.InjectPlugin;

/**
 * Use this annotation to mark functions which should be called on initialization. You
 * can be sure that at this phase all required plugins (see &#064;{@link InjectPlugin}),
 * that have not been marked as optional, are available. So, for 
 * example, if you want a method to be called when the plugin is ready, you could 
 * write:<br/><br/>
 * 
 * <code>
 * &#064;Init<br/>
 * public void startup() { ... }
 * </code><br/><br/>
 * 
 * This method usually does not have to return a value (return type <code>void</code>). 
 * It may, however, return as well a boolean. If it then returns <code>false</code>, initialization of 
 * this plugin will be canceled, no &#064;{@link com.avairebot.base.annotations.Thread} or &#064;{@link Timer} will be started, no other
 * &#064;{@link Init} methods will be called and the plugin will not be touched any more. <br/><br/>
 * 
 * Note: Methods annotated with this have to be PUBLIC, otherwise they won't be found.
 *
 * @author Ralf Biedert
 * @see java.lang.Shutdown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Init {
    // Just a marker interface
}
