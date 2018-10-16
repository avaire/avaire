/*
 * Version.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
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
package com.avairebot.base.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avairebot.base.PluginInformation;
import com.avairebot.base.PluginInformation;

/**
 * Version of the given plugin. For example, to specify that a given 
 * plugin is version 1.3.2, write:<br/><br/>
 * 
 * <code>
 * &#064;Version(version = 10302)<br/>
 * &#064;PluginImplementation<br/>
 * public class ServiceImpl implements Service { ... } 
 * </code><br/><br/>
 * 
 * This information can be queried using the {@link PluginInformation} plugin.
 * 
 * @author Ralf Biedert
 * @see PluginInformation.Information
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Version {

    /** Major version */
    public final static int UNIT_MAJOR = 10000;

    /** Minor version */
    public final static int UNIT_MINOR = 100;

    /** Release version */
    public final static int UNIT_RELEASE = 1;

    /**
     * Version of this plugin. For example, a version of 10000 should be read as 1.00.00
     * 
     * @return .
     */
    int version() default 1 * UNIT_MAJOR + 0 * UNIT_MINOR + 0 * UNIT_RELEASE;
}
