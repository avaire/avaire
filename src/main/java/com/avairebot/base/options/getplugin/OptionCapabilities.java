/*
 * OptionDummy.java
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
package com.avairebot.base.options.getplugin;

import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.options.GetPluginOption;
import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.options.GetPluginOption;

/**
 * Specifies the method should only consider plugins satisfying all the 
 * given {@link Capabilities}. This is useful in case there are a number of plugins
 * implementing the same interface. With this option, only plugins with the 
 * specified capabilities are considered. For example, to get a plugin implementing
 * the <code>Language</code> and capable of handling English, write:<br/><br/>
 * 
 * <code>
 * backendPluginManager.getPlugin(Language.class, new OptionCapabilities("language:english"));
 * </code><br/><br/>
 * 
 * If multiple capabilities are specified only plugins matching all of them are being 
 * considered. Multiple capabilities MUST be specified within a single option, not as
 * multiple options, i.e., write:<br/><br/>
 * 
 * <code>
 * new OptionCapabilities("filetype:xml", "filetype:csv", "filetype:raw");
 * </code><br/><br/>
 * 
 * @author Ralf Biedert
 */
public class OptionCapabilities implements GetPluginOption {

    /** */
    private static final long serialVersionUID = -7856506348748868122L;

    /** */
    private String[] caps;

    /**
     * Returns plugins that matches all given capabilites. 
     * 
     * @param matchingCapabilites The capabilities to consider.
     */
    public OptionCapabilities(String... matchingCapabilites) {
        this.caps = matchingCapabilites;
    }

    /**
     * Returns the requested capabilities. 
     * 
     * @return Array of caps. 
     */
    public String[] getCapabilities() {
        return this.caps;
    }
}
