/*
 * PluginClassMetaInformation.java
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
package com.avairebot.base.impl.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.avairebot.base.Plugin;

/**
 * Meta information of the given plugin class.
 * 
 * @author Ralf Biedert
 */
public class PluginClassMetaInformation {

    /**
     * @author rb
     */
    public static enum PluginClassStatus {
        /** No further information are available. Should not be observed 
         * under normal circumstances. */
        UNDEFINED,

        /** Plugin has been accepted as a valid plugin */
        ACCEPTED,

        /** Disabled due to various reasons (check logging output). */
        DISABLED,

        /** Plugin contains unresolved dependencies */
        CONTAINS_UNRESOLVED_DEPENDENCIES,

        /** Plugin is ready for spawning, should happen soon. */
        SPAWNABLE,

        /** Plugin has been lazy-spawned. ??? */
        LAZY_SPAWNED,

        /** Plugin has been spawned. Should be accessible now 
         * by getPlugin(). */
        SPAWNED,

        /** If the class failed to spawn */
        FAILED,
    }

    /**
     * Another plugin this plugin depends on.
     * 
     * @author rb
     *
     */
    public static class Dependency {
        /** */
        public Class<? extends Plugin> pluginClass;

        /** */
        public String[] capabilites = new String[0];

        /** */
        public boolean isOptional = false;
    }

    /** Status of this plugin class */
    public PluginClassStatus pluginClassStatus = PluginClassStatus.UNDEFINED;

    /** Where this plugin came from */
    public URI pluginOrigin;

    /** The dependencies of this class */
    public Collection<Dependency> dependencies = new ArrayList<Dependency>();

}
