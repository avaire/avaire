/*
 * InjectPluginManager.java
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
package com.avairebot.base.annotations.injections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.annotations.Capabilities;

/**
 * Inject some instance implementing the given plugin interface. If the plugin is not available 
 * at spawntime (and the dependency is marked as optional), null will be inserted. If the plugin
 * was not marked as optional this plugin will not be spawned. For example,
 * to specify that the BackendPluginManager should be injected into the plugin, you would write:<br/><br/>
 * 
 * <code>
 * &#064;InjectPlugin<br/>
 * public BackendPluginManager backendPluginManager;
 * </code><br/><br/>
 * 
 * Another example. To specify that a language service for Swahili should be injected in case it 
 * is there and null in case it is not, you could write:<br/><br/>
 * 
 * <code>
 * &#064;InjectPlugin(requiredCapabilities = {"language:swahili"}, isOptional=true)<br/>
 * public LanguageService service;
 * </code><br/><br/>
 *   
 * This ensures that the returned plugin, if it is there, is of type <code>LanguageService</code> and has the 
 * capability (see {@link Capabilities}) of processing Swahili. If <code>isOptional</code> is <code>false</code> 
 * or omitted then it is even ensured that this plugin will not be spawned unless the given service is available.
 * <br/><br/>
 * 
 * Please note: The annotated variable has to be <b>public</b>!
 *
 * @author Ralf Biedert
 * @see BackendPluginManager
 *
 */
@Target(value = { ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectPlugin {

    /**
     * All the capabilities the plugins must have in order to be instanciated 
     * 
     * @return Number of classes that the plugin explicitly depends on.
     */
    String[] requiredCapabilities() default {};

    /**
     * If set to true, the BackendPluginManager may instanciate the plugin anyway, even if the
     * other plugin is not present yet. 
     * 
     * @return .
     */
    boolean isOptional() default false;

}
