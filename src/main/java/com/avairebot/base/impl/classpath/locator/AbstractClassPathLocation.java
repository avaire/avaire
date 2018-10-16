/*
 * ClasspathLoaction.java
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
package com.avairebot.base.impl.classpath.locator;

import static net.jcores.CoreKeeper.$;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;

import com.avairebot.base.impl.classpath.locator.locations.FileClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.JARClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.MultiPluginClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.JARClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.MultiPluginClasspathLocation;
import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.cache.JARCache.JARInformation;
import com.avairebot.base.impl.classpath.locator.locations.FileClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.JARClasspathLocation;
import com.avairebot.base.impl.classpath.locator.locations.MultiPluginClasspathLocation;

/**
 * Location of a classpath (i.e., either a JAR file or a toplevel directory)
 * 
 * TODO: Constrict two subclasses, JARClassPathLocation and FileClassPathLocation
 * 
 * @author Ralf Biedert
 * 
 */
public abstract class AbstractClassPathLocation {
    /**
     * 
     * Type of this location
     * 
     * @author Ralf Biedert
     * 
     */
    public enum LocationType {
        /** Is a JAR */
        JAR,
        /** Is an ordinary dir */
        DIRECTORY,
        /** A multiplugin*/
        MULTI_PLUGIN
    }

    /** */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Location of this item */
    protected final URI location;

    /** ID of this entry */
    protected final String realm;

    /** */
    protected final JARCache cache;

    /** Information for this location entry */
    protected JARInformation cacheEntry = null;

    /**
     * @param cache
     * @param realm
     * @param location
     */
    protected AbstractClassPathLocation(JARCache cache, String realm, URI location) {
        this.cache = cache;
        this.realm = realm;
        this.location = location;
    }

    /**
     * Constructs a new ClassPathLocation which handles all classes within.  
     * 
     * @param cache The cache to lookup the entrie's content.
     * @param realm The real name.
     * @param location URI location of the given classpath.
     * @return The constructed location.
     */
    public static AbstractClassPathLocation newClasspathLocation(JARCache cache,
                                                                 String realm,
                                                                 URI location) {
        if ($(location).filter(".*\\.plugin[/]$").get(0) != null)
            return new MultiPluginClasspathLocation(cache, realm, location);
        if (location.toString().endsWith(".jar"))
            return new JARClasspathLocation(cache, realm, location);

        return new FileClasspathLocation(cache, realm, location);
    }

    /**
     * Returns the top level classpath location. This is *NOT* equal to the 
     * the classpath-entries this location provides. Especially multi-plugins
     * may consist of a number of JARs required for proper class resolution. 
     * 
     * @return The top level location
     */
    public URI getToplevelLocation() {
        return this.location;
    }

    /**
     * Gets all classpath entries required to properly load plugins. Add them
     * to a class loader.
     * 
     * @return the location
     */
    public URI[] getClasspathLocations() {
        return new URI[] { this.location };
    }

    /**
     * @return the location
     */
    public String getRealm() {
        return this.realm;
    }

    /**
     * Get the type of this entry
     * 
     * @return .
     */
    public abstract LocationType getType();

    /**
     * Lists the name of all classes inside this classpath element
     * 
     * @return .
     */
    public abstract Collection<String> listToplevelClassNames();

    /**
     * Lists all entries in this location, no matter if class or file (excluding directories)
     * 
     * @return .
     */
    public abstract Collection<String> listAllEntries();

    /**
     * Creates an input stream for the requested item
     * 
     * @param entry
     * 
     * @return .
     */
    public abstract InputStream getInputStream(String entry);

}
