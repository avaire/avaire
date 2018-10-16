/*
 * ClasspathLocator.java
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.util.PluginConfigurationUtil;
import com.avairebot.base.impl.PluginManagerImpl;
import com.avairebot.base.impl.classpath.cache.JARCache;

/**
 * Used to find classpaths, JARs and their contents.
 * 
 * @author Ralf Biedert
 */
public class ClassPathLocator {

    /** For debugging output */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Cache to lookup elements */
    private final JARCache cache;

    /** Mainly used to access the config. */
    private PluginManagerImpl pluginManager;

    /**
     * @param pluginManager
     * @param cache
     */
    public ClassPathLocator(PluginManagerImpl pluginManager, JARCache cache) {
        this.pluginManager = pluginManager;
        this.cache = cache;
    }

    /**
     * Given a top level entry, finds a list of class path locations below the given
     * entry. The top level entry can either be a folder, or it can be a JAR directly.
     * 
     * @param toplevel The top level URI to start from.
     * @return A list of class path locations.
     */
    public Collection<AbstractClassPathLocation> findBelow(URI toplevel) {

        final Collection<AbstractClassPathLocation> rval = new ArrayList<AbstractClassPathLocation>();
        final File startPoint = new File(toplevel);

        // First, check if the entry represents a multi-plugin (in that case we don't add
        // anything else)
        if ($(startPoint).filter(".*\\.plugin?$").get(0) != null) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        // Check if this is a directory or a file
        if (startPoint.isDirectory()) {
            final File[] listFiles = startPoint.listFiles();

            boolean hasJARs = false;

            for (File file : listFiles) {
                if (file.getAbsolutePath().endsWith(".jar")) {
                    rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, file.toURI().toString(), file.toURI()));
                    hasJARs = true;
                }

                if ($(file).filter(".*\\.plugin?$").get(0) != null) {
                    rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, file.toURI().toString(), file.toURI()));
                    hasJARs = true;
                }
            }

            // If we have JARs, we already added them
            if (hasJARs) return rval;

            // If we have no JARs, this is probably a classpath, in this case warn that
            // the method is not recommended
            if (toplevel.toString().contains("/bin/") || toplevel.toString().contains("class")) {
                this.logger.warning("Adding plugins in 'raw' classpaths, such as 'bin/' or 'classes/' is not recommended. Please use classpath://* instead (the video is a bit outdated in this respect).");
            }
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        // If this is directly a JAR, add this
        if (startPoint.isFile() && startPoint.getAbsolutePath().endsWith(".jar")) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        return rval;
    }

    /**
     * Finds all locations inside the current classpath.
     * 
     * @return .
     */
    @SuppressWarnings("boxing")
    public Collection<AbstractClassPathLocation> findInCurrentClassPath() {
        final Collection<AbstractClassPathLocation> rval = new ArrayList<AbstractClassPathLocation>();

        // Get our current classpath (TODO: Better get this using
        // ClassLoader.getSystemClassLoader()?)
        final boolean filter = new PluginConfigurationUtil(this.pluginManager.getPluginConfiguration()).getBoolean(BackendPluginManager.class, "classpath.filter.default.enabled", true);
        final String blacklist[] = new PluginConfigurationUtil(this.pluginManager.getPluginConfiguration()).getString(BackendPluginManager.class, "classpath.filter.default.pattern", "/jre/lib/;/jdk/lib/;/lib/rt.jar").split(";");
        final String pathSep = System.getProperty("path.separator");
        final String classpath = System.getProperty("java.class.path");
        final String[] split = classpath.split(pathSep);
        final List<URL> toFilter = new ArrayList<URL>();

        this.logger.fine("Finding classes in current classpath (using separator '" + pathSep + "'): " + classpath);

        // Check if we should filter, if yes, get topmost classloader so we know
        // what to filter out
        if (filter) {
            this.logger.finer("Filtering default classpaths by request.");

            ClassLoader loader = ClassLoader.getSystemClassLoader();
            while (loader != null && loader.getParent() != null)
                loader = loader.getParent();

            // Get 'blacklist' and add it to our filterlist
            if (loader != null && loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                for (URL url : urls) {
                    this.logger.finer("Putting '" + url + "' on our filterlist.");
                    toFilter.add(url);
                }
            }

            // Print blacklisted elements
            for (String item : blacklist) {
                this.logger.finer("Blacklist entry: " + item);
            }
        }

        // Process all possible locations
        for (String string : split) {
            try {
                final URL url = new File(string).toURI().toURL();

                this.logger.fine("Trying to add '" + string + "' to our classpath location.");
                this.logger.fine("Converted to " + url);

                // Check if the url was already contained
                if (toFilter.contains(url) || blacklisted(blacklist, url)) {
                    this.logger.fine("But it was filtered because it was in our list or blacklisted.");
                    continue;
                }

                // And eventually add the location
                rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, "#classpath", new File(string).toURI()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return rval;
    }

    /**
     * Checks if the given URL is blacklisted
     * 
     * @param blacklist
     * @param url
     * @return
     */
    private boolean blacklisted(String[] blacklist, URL url) {
        // Default sanity check
        if (blacklist == null || blacklist.length == 0 || blacklist[0].length() == 0)
            return false;

        // Go thorugh blacklist
        for (String string : blacklist) {
            if (url.toString().contains(string)) return true;
        }

        return false;
    }
}
