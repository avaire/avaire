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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avairebot.base.annotations.events.Init;
import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.options.getplugin.OptionCapabilities;

/**
 * Methods marked with Capabilities are queried by PluginInformation. The method MUST be
 * public and it MUST return a String[] array. If several methods are annotated it is
 * undefined which one will be called. <br/><br/>
 * 
 * As soon as the &#064;{@link Init} annotation is processed the capabilities function must be
 * operational. Use this function for example to tell which file extensions you can handle 
 * or the (limited number of) host you can connect to. While this method can return different 
 * values on every call it is considered "bad taste" if latter calls return anything less 
 * than the previous calls.<br/><br/>
 * 
 * For example, if you want a plugin to indicate that it can handle a set of languages,
 * you could write:<br/><br/>
 * 
 * <code>
 * &#064;Capabilities<br/>
 * public String[] capabilities() { return new String[] {"language:english", "language:german"}; }
 * </code><br/><br/>
 * 
 * Later on, you can use {@link OptionCapabilities} within the {@link BackendPluginManager}'s <code>getPlugin()</code> method to
 * retrieve all plugins providing certain capabilities.    
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Capabilities {
    //
}
