/*
 * InternalClasspathLoader.java
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
package com.avairebot.base.impl.classpath.loader;

import java.net.URI;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avairebot.AvaIre;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.locator.ClassPathLocator;
import com.avairebot.base.impl.classpath.ClassPathManager;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.locator.ClassPathLocator;
import com.avairebot.base.Plugin;
import com.avairebot.base.impl.PluginManagerImpl;
import com.avairebot.base.impl.classpath.ClassPathManager;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.locator.ClassPathLocator;

/**
 * A loader to handle classpath://* URIs.
 * 
 * @author Ralf Biedert
 */
public class InternalClasspathLoader extends AbstractLoader {

    /**
     * @param pluginManager
     */
    public InternalClasspathLoader(PluginManagerImpl pluginManager, AvaIre bot) {
        super(pluginManager, bot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.impl.loader.AbstractLoader#handlesURI(java.net.URI)
     */
    @Override
    public boolean handlesURI(URI uri) {
        if (uri.getScheme().equals("classpath")) return true;

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.impl.loader.AbstractLoader#loadFrom(java.net.URI)
     */
    @Override
    public void loadFrom(URI url) {

        // Special handler to load files from the local classpath
        if (url.toString().contains("*")) {
            if (url.toString().equals("classpath://*")) {
                loadAllClasspathPluginClasses(null);
            } else {
                String pattern = url.toString();
                pattern = pattern.replace("**", ".+");
                pattern = pattern.replace("*", "[^\\.]*");
                pattern = pattern.replace("classpath://", "");
                loadAllClasspathPluginClasses(pattern);
            }
            return;
        }

        // Special handler to load files from the local classpath, specified by name.
        // Please note that this is a very bad solution and should only be used in special
        // cases,
        // as when invoking from applets that don't have permission to access the
        // classpath (is this so)
        if (url.toString().startsWith("classpath://")) {
            // Obtain the fq-classname to load
            final String toLoad = url.toString().substring("classpath://".length());

            // Try to load all plugins, might cause memory problems due to Issue #20.
            try {
                loadClassFromClasspathByName(toLoad);
            } catch (OutOfMemoryError error) {
                this.logger.severe("Due to a bug (Issue #20), JSPF ran low on memory. Please increase your memory by (e.g., -Xmx1024m) or specify a 'classpath.filter.default.pattern' options. We hope to fix this bux in some future release. We are sorry for the inconvenience this might cause and we can understand if you hate us now :-(.");
                error.printStackTrace();
            }

            return;
        }
    }

    /**
     * Load all plugins from the classpath that match a given pattern.
     * 
     * @param pattern
     */
    private void loadAllClasspathPluginClasses(String pattern) {
        // Start the classpath search
        this.logger.finer("Starting classpath search with pattern " + pattern);

        // Get all classpath locations of the current classpath
        final ClassPathManager manager = this.pluginManager.getClassPathManager();
        final ClassPathLocator locator = manager.getLocator();
        final Collection<AbstractClassPathLocation> locations = locator.findInCurrentClassPath();

        // Process all locations
        for (AbstractClassPathLocation location : locations) {
            manager.registerLocation(location);

            final Collection<String> candidates = manager.findSubclassesFor(location, Plugin.class);

            this.logger.finer("Found " + candidates.size() + " candidates.");

            // Check all candidates
            for (String string : candidates) {

                // Either try to add them all, or only those who match a given pattern
                if (pattern == null) tryToLoadClassAsPlugin(location, string, bot);
                else {
                    final Pattern p = Pattern.compile(pattern);
                    final Matcher m = p.matcher(string);

                    this.logger.finest(string + " " + m.matches());
                    if (m.matches()) {
                        tryToLoadClassAsPlugin(location, string, bot);
                    }
                }
            }
        }

        return;
    }

    private void loadClassFromClasspathByName(final String toLoad) {
        this.logger.fine("Loading " + toLoad + " directly");
        tryToLoadClassAsPlugin(null, toLoad, bot);
    }
}
