/*
 * ClassURI.java
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
package com.avairebot.base.util.uri;

import java.net.URI;
import java.net.URISyntaxException;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.Plugin;

/**
 * Convenience method to load plugins from the classpath
 * 
 * @author Ralf Biedert
 * @see BackendPluginManager
 */
public class ClassURI extends URIUtil {
    /** Means we should add all plugins we have in our classpath */
    public static final URI CLASSPATH = URI.create("classpath://*");

    /**
     * Specifies a pattern to add from the current classpath.
     * 
     * @param pattern For example net.xeoh.myplugins.**
     * 
     * @return The generated pattern.
     */
    public static final URI CLASSPATH(String pattern) {
        return URI.create("classpath://" + pattern);
    }
    
    
    /**
     * Specifies that the given plugin should be added.
     * 
     * @param clazz The plugin to add

     * @return The generated URI pattern for the plugin.
     */
    public static final URI PLUGIN(Class<? extends Plugin> clazz) {
        return new ClassURI(clazz).toURI();
    }
    

    /** The class we wrapped */
    private final Class<? extends Plugin> clazz;

    /**
     * Construct a new class URI for the given class.
     * 
     * @param clazz The class to wrap.
     */
    public ClassURI(Class<? extends Plugin> clazz) {
        if (clazz.isInterface())
            throw new IllegalArgumentException("The paramter must be a concrete plugin class, not a plugin interface.");

        this.clazz = clazz;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.util.uri.URIUtil#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI("classpath://" + this.clazz.getCanonicalName());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

}
