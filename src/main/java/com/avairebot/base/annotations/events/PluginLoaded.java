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
 */
package com.avairebot.base.annotations.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.annotations.injections.InjectPlugin;

/**
 * 
 * Called when a plugin of the given interface was registered. For example, if you want to 
 * be called when a new Plugin of the type <code>StorageService</code> was added, you could 
 * write:<br/><br/>
 * 
 * <code>
 * &#064;PluginLoaded<br/>
 * public void checkPlugin(StorageService service) { ... }
 * </code><br/><br/>
 * 
 * This method is especially useful in the case of plugins added <i>in the future</i>. Sometimes
 * you rely on a plugin which is not there in the early stage of your application lifetime. If you 
 * depended on it with {@link InjectPlugin}, you either would not see it, or your own plugin would
 * be suspended until it was loaded. Using the <code>PluginLoaded</code> you can start up anyway
 * and wait for it in this callback-like mechanism.<br/><br/> 
 *    
 * Note: Methods annotated with this have to be PUBLIC, otherwise they won't be found.
 *
 * @author Ralf Biedert
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PluginLoaded {
    //
}
