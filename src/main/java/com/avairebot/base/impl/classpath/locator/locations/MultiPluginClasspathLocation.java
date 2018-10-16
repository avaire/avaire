/*
 * JARClasspathLocation.java
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
package com.avairebot.base.impl.classpath.locator.locations;

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;

/**
 * A multi-plugin in a meta-plugin containing several sub-plugins sharing the same class 
 * loader and have an external dependency folder. 
 * 
 * @author Ralf Biedert
 */
public class MultiPluginClasspathLocation extends AbstractClassPathLocation {

    /** Maps our returned entries to contained JARs (so we know where too look if we want to resolve them) */
    final Map<String, String> entryMapping = new HashMap<String, String>();

    /** All the JARs we handle */
    final Collection<String> allJARs;

    /**
     * @param cache
     * @param realm
     * @param location
     */
    public MultiPluginClasspathLocation(JARCache cache, String realm, URI location) {
        super(cache, realm, location);

        this.allJARs = $(location).file().dir().filter(".*jar$").string().list();
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#getType()
     */
    @Override
    public LocationType getType() {
        return LocationType.MULTI_PLUGIN;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#getClasspathLocations()
     */
    @Override
    public URI[] getClasspathLocations() {
        return $(this.allJARs).string().file().uri().array(URI.class);
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#getInputStream(java.lang.String)
     */
    @Override
    public InputStream getInputStream(String file) {
        final String uri = this.entryMapping.get(file);

        if (uri == null) return null;

        try {
            final JarURLConnection connection = (JarURLConnection) new URI("jar:" + new File(uri).toURI() + "!/").toURL().openConnection();
            final JarFile jarFile = connection.getJarFile();

            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();

                // We only search for class file entries
                if (entry.isDirectory()) continue;

                String name = entry.getName();

                if (name.equals(file)) return jarFile.getInputStream(entry);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#listAllEntries()
     */
    @Override
    public Collection<String> listAllEntries() {
        ArrayList<String> rval = new ArrayList<String>();

        for (String entry : this.allJARs) {
            final Collection<String> all = JARClasspathLocation.listAllEntriesFor(URI.create(entry));
            for (String string : all) {
                this.entryMapping.put(string, entry);
            }

            rval.addAll(all);
        }

        return rval;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#listToplevelClassNames()
     */
    @Override
    public Collection<String> listToplevelClassNames() {
        ArrayList<String> rval = new ArrayList<String>();

        for (String entry : this.allJARs) {
            final Collection<String> all = JARClasspathLocation.listToplevelClassNamesForURI(new File(entry).toURI());
            for (String string : all) {
                this.entryMapping.put(string, entry);
            }

            rval.addAll(all);
        }

        return rval;
    }

}
