/*
 * PluginInformation.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
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
package com.avairebot.base;

import java.util.Collection;

import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.annotations.meta.Version;
import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.annotations.meta.Version;

/**
 * Returns various information about plugins, static as well as dynamic. 
 *
 * @author Ralf Biedert
 */
public interface PluginInformation extends Plugin {
    /**
     * The set of information item that can be requested. <b>S1</b> means a list 
     * of strings with exactly one element is returnd, <b>S+</b> means a number of 
     * strings will be returned. In case no information was available an empty 
     * collection is returned. 
     * 
     * @author Ralf Biedert
     */
    public static enum Information {
        /** 
         * The author of this plugins (S1). 
         *
         *  @see Author
         */
        AUTHORS,

        /**
         * Returns the self proclaimed capabilites of this plugin (S+). 
         * 
         * @see Capabilities
         */
        CAPABILITIES,

        /**
         * Version of this plugin (S1). A version number of 10304 will be 
         * returned as 1.03.04.
         * 
         *  @see Version
         */
        VERSION,

        /** 
         * Date when the plugin was initialized. The unix time will be returned (S1).<br/><br/>
         * 
         * TODO: Not implemented yet.
         */
        INIT_DATE,

        /**
         * Returns a single string containing the URI to the classpath item this 
         * element came from (S1).
         */
        CLASSPATH_ORIGIN,
    }

    /**
     * Returns an {@link Information} item about a plugin. For example, to query a plugin's 
     * classpath origin you would write:<br/><br/>
     * 
     * <code>
     * getInformation(Information.CLASSPATH_ORIGIN, plugin);
     * </code>
     * 
     * @param item The information item to request. 
     * @param plugin The plugin for which the information is requested.
     *
     * @return A collection of strings containing the requested information. The the specific {@link Information} 
     * item for more details. If nothing sensible was found, an empty collection is returned.
     */
    public Collection<String> getInformation(Information item, Plugin plugin);
}
