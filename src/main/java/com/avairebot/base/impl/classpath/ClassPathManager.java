/*
 * ClassPathManager.java
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
package com.avairebot.base.impl.classpath;

import com.avairebot.AvaIre;
import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.loader.AbstractLoader;
import com.avairebot.base.impl.classpath.loader.FileLoader;
import com.avairebot.base.impl.classpath.loader.HTTPLoader;
import com.avairebot.base.impl.classpath.loader.InternalClasspathLoader;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.locator.ClassPathLocator;
import com.avairebot.base.impl.classpath.locator.locations.JARClasspathLocation;
import com.avairebot.base.impl.PluginManagerImpl;
import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.cache.JARCache.JARInformation;
import com.avairebot.base.impl.classpath.loader.AbstractLoader;
import com.avairebot.base.impl.classpath.loader.FileLoader;
import com.avairebot.base.impl.classpath.loader.HTTPLoader;
import com.avairebot.base.impl.classpath.loader.InternalClasspathLoader;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.locator.ClassPathLocator;
import com.avairebot.base.impl.classpath.locator.locations.JARClasspathLocation;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.classworlds.NoSuchRealmException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Manages all our classpaths shared by different plugins.
 * 
 * @author Ralf Biedert
 */
public class ClassPathManager {
    /** Console and file logging */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Blocks access to the file cache */
    private final Lock cacheLock = new ReentrantLock();

    /** Manages content cache of jar files */
    private final JARCache jarCache = new JARCache();

    /** Locates possible classpaths */
    private final ClassPathLocator locator;

    /** Loads plugins from various urls */
    private final Collection<AbstractLoader> pluginLoader = new ArrayList<AbstractLoader>();

    /** Manages classpaths from URLs */
    ClassWorld classWorld;

    /**
     * Indicates if we're initialized properly (application mode) or if we had sandbox
     * problems (applet mode).
     */
    boolean initializedProperly = false;

    /**
     * @param pluginManager
     */
    @SuppressWarnings("synthetic-access")
    public ClassPathManager(PluginManagerImpl pluginManager, AvaIre bot) {
        this.locator = new ClassPathLocator(pluginManager, this.jarCache);

        // Register loader
        this.pluginLoader.add(new InternalClasspathLoader(pluginManager, bot));
        this.pluginLoader.add(new FileLoader(pluginManager, bot));
        this.pluginLoader.add(new HTTPLoader(pluginManager, bot));

        // Initialization is a bit ugly, but we might be in a sandbox
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    // Try to create a new ClassWorld object
                    ClassPathManager.this.classWorld = new ClassWorld();

                    // Create a core realm (for internal and classpath://* plugins)
                    try {
                        ClassPathManager.this.classWorld.newRealm("core", getClass().getClassLoader());
                    } catch (DuplicateRealmException e) {
                        e.printStackTrace();
                    }

                    // Signal that we are okay.
                    ClassPathManager.this.initializedProperly = true;
                } catch (SecurityException e) {
                    ClassPathManager.this.logger.warning("Proper initialization failed due to security restrictions. Only classpath://xxx URIs will work. Sorry.");
                }
                return null;
            }
        });
    }

    /**
     * Locates plugins at a given source, loads them and adds them to the registry.
     * 
     * @param location
     * @return .
     */
    public boolean addFromLocation(URI location) {
        this.cacheLock.lock();
        try {
            // Load local cache
            this.jarCache.loadCache();

            // Handle URI
            for (AbstractLoader loader : this.pluginLoader) {
                if (!loader.handlesURI(location)) continue;
                loader.loadFrom(location);
                return true;
            }
        } finally {
            this.jarCache.saveCache();
            this.cacheLock.unlock();
        }

        return false;
    }

    /**
     * Loads a class given its name and classpath location.
     * 
     * @param location Specifies where this plugins should be obtained from, or
     * <code>null</code> if we should use our own classloader.
     * @param name The name of the class to load.
     * 
     * @return The requested class.
     * 
     * @throws ClassNotFoundException
     */
    public Class<?> loadClass(AbstractClassPathLocation location, String name)
                                                                              throws ClassNotFoundException {
        // In case no location is supplied ...
        if (location == null) { return getClass().getClassLoader().loadClass(name); }

        try {
            if (this.initializedProperly) {
                final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
                return classLoader.loadClass(name);
            }
        } catch (ClassNotFoundException e) {
            return getClass().getClassLoader().loadClass(name);
        } catch (NoSuchRealmException e) {
            e.printStackTrace();
        }

        // And again, this time we run this code if we have not been inititalized properly
        return getClass().getClassLoader().loadClass(name);
    }

    /**
     * Finds all subclasses for the given superclass.
     * 
     * @param location The location to search for.
     * @param superclass The superclass to obtain subclasses for.
     * 
     * @return A list of plugin names extending <code>superclass</code>.
     */
    public Collection<String> findSubclassesFor(AbstractClassPathLocation location,
                                                Class<?> superclass) {

        final Collection<String> rval = new ArrayList<String>();
        if (!this.initializedProperly) return rval;

        // Check if we can get the requested information out of the cache
        JARCache.JARInformation cacheEntry = null;

        // If it is a JAR entry, check if we have cache information
        if (location instanceof JARClasspathLocation) {
            cacheEntry = ((JARClasspathLocation) location).getCacheEntry();

            if (cacheEntry != null) {
                final Collection<String> collection = cacheEntry.subclasses.get(superclass.getCanonicalName());
                if (collection != null) return collection;
            }
        }

        // No? Okay, search the hard way ...
        try {
            final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
            final Collection<String> listClassNames = location.listToplevelClassNames();

            for (String name : listClassNames) {
                try {
                    final Class<?> c = Class.forName(name, false, classLoader);

                    // No interfaces please
                    if (c.isInterface()) continue;

                    if (superclass.isAssignableFrom(c) && !superclass.getCanonicalName().equals(c.getCanonicalName())) {
                        rval.add(name);
                    }
                } catch (ClassNotFoundException e) {
                    this.logger.fine("ClassNotFoundException. Unable to inspect class " + name + " although it appears to be one.");

                    // Print all causes, helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                } catch (final NoClassDefFoundError e) {
                    this.logger.finer("Ignored class " + name + " due to unresolved dependencies");

                    // Print all causes, helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                } catch (SecurityException e) {
                    this.logger.fine("SecurityException while trying to find subclasses. Cause of trouble: " + name + ". This does not neccessarily mean problems however.");

                    // Print all causes, helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                }
            }
        } catch (NoSuchRealmException e1) {
            e1.printStackTrace();
        }

        // Update the cache information
        if (cacheEntry != null) {
            cacheEntry.subclasses.put(superclass.getCanonicalName(), rval);
        }

        return rval;
    }

    /**
     * Adds a classpath location to this manager.
     * 
     * @param location
     */
    public void registerLocation(AbstractClassPathLocation location) {
        if (!this.initializedProperly) return;

        try {
            final ClassRealm newRealm = this.classWorld.newRealm(location.getRealm(), getClass().getClassLoader());
            final URI[] classpathLocations = location.getClasspathLocations();
            for (URI uri : classpathLocations) {
                newRealm.addConstituent(uri.toURL());
            }

        } catch (DuplicateRealmException e) {
            // Happens for #classpath realms ...
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a resource as an input stream for a given location.
     * 
     * @param location The location to use.
     * @param name The requested resource.
     * @return The input stream for the requested resource or <code>null</code> if none
     * was found.
     */
    public InputStream getResourceAsStream(AbstractClassPathLocation location, String name) {
        // In case no location is supplied ...
        if (location == null) { return getClass().getClassLoader().getResourceAsStream(name); }

        try {
            final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
            return classLoader.getResourceAsStream(name);
        } catch (NoSuchRealmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns our locator.
     * 
     * @return The locator.
     */
    public ClassPathLocator getLocator() {
        return this.locator;
    }

    /**
     * Returns the JAR cache.
     * 
     * @return The cache.
     */
    public JARCache getCache() {
        return this.jarCache;
    }
}
