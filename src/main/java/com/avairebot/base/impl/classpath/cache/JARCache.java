/*
 * JARCache.java
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
package com.avairebot.base.impl.classpath.cache;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Information about JAR files
 * 
 * @author rb
 */
public class JARCache {

    /**
     * Information about a JAR plugin container.
     * 
     * @author Ralf Biedert
     * 
     */
    public static class JARInformation implements Serializable {
        /** */
        private static final long serialVersionUID = 6734024814836912079L;

        /** List of valid classes in the jar */
        public List<String> classes = new ArrayList<String>();

        /** List of valid plugins in the jar */
        public Map<String, Collection<String>> subclasses = new HashMap<String, Collection<String>>();

        /** */
        public long lastAccess = System.currentTimeMillis();

        /** */
        public int usageCount = 0;

        /** Is the contents information valid? */
        public boolean classesValid = false;

    }

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Name of the default cache file */
    private final static String DEFAULT_CACHE_FILE = "jspf.plugin.cache";

    /** Is the cache enabled */
    private boolean cacheEnabled = false;

    /** maps a fingerprint to a jar information */
    private Map<String, JARInformation> cacheMap = new HashMap<String, JARInformation>();

    /** */
    private boolean weakMode = false;

    private String cachePath;

    /**
     * Load cache
     */
    @SuppressWarnings("unchecked")
    public void loadCache() {
        if (!this.cacheEnabled) return;

        final String cacheFile = (this.cachePath == null) ? DEFAULT_CACHE_FILE : this.cachePath;

        try {
            // lock the lockfile
            final FileInputStream fis = new FileInputStream(cacheFile);
            final ObjectInputStream ois = new ObjectInputStream(fis);
            final Object rval = ois.readObject(); // Loading caches can take very long
                                                  // (>250ms) for many entries

            ois.close();

            if (rval != null) {
                this.cacheMap = (Map<String, JARInformation>) rval;
            }
        } catch (EOFException e) {
            //
        } catch (final FileNotFoundException e) {
            // If file was not found, no problem ...
            // e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            this.logger.warning("Your JSPF cache is outdated, please delete it. It will be regenerated with the next run. The next exception reflects this, so don't be afraid.");
            e.printStackTrace();
        }
    }

    /**
     * @param uri
     * @return .
     */
    public JARInformation getJARInformationFor(final URI uri) {
        // Just return a dummy when we are disabled
        if (!this.cacheEnabled) return new JARInformation();

        final File file = new File(uri);
        final String hash = (this.weakMode) ? generateWeakHash(file) : generateStrongHash(file);

        if (hash == null) {
            this.logger.warning("Error generating hash. Caching won't work.");
            return new JARInformation();
        }

        // Create information if not already there.
        if (!this.cacheMap.containsKey(hash)) {
            this.cacheMap.put(hash, new JARInformation());
        }

        final JARInformation jarInformation = this.cacheMap.get(hash);
        jarInformation.lastAccess = System.currentTimeMillis();
        jarInformation.usageCount++;
        return jarInformation;
    }

    /**
     * Saves the cache
     */
    public void saveCache() {

        if (!this.cacheEnabled) return;
        String cacheFile = this.cachePath;
        // String = this.configuration.getConfiguration(getClass(), "cacheFile");
        if (cacheFile == null) {
            cacheFile = DEFAULT_CACHE_FILE;
        }

        try {
            // lock the lockfile
            final FileOutputStream fos = new FileOutputStream(cacheFile);
            final ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this.cacheMap);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Weak hash mode.
     * 
     * @param file
     * @return
     */
    private String generateWeakHash(File file) {
        final String name = file.getName();
        final long length = file.length();

        final String hash = "weak:" + name + "@" + length;
        return hash;
    }

    /**
     * Generate a hash for the given element.
     * 
     * @param element
     * 
     * @return .
     */
    @SuppressWarnings("boxing")
    private String generateStrongHash(final File element) {
        // Try to generate hash

        boolean created = false;

        try {
            this.logger.fine("Processing JAR " + element);

            final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            final FileInputStream fis = new FileInputStream(element);

            // Read Data
            final byte[] data = new byte[1024 * 1024];
            int avail = fis.available();

            // Update hash
            while (avail > 0) {
                avail = Math.min(avail, data.length);

                fis.read(data, 0, avail);

                digest.update(data, 0, avail);
                avail = fis.available();
            }

            final byte[] hash = digest.digest();

            // Assemble hash string
            final StringBuilder sb = new StringBuilder();
            sb.append("md5:");
            for (final byte b : hash) {
                final String format = String.format("%02x", b);
                sb.append(format);
            }

            fis.close();

            final String hashValue = sb.toString().substring(0, sb.toString().length());
            created = true;
            this.logger.fine("Hash of " + element + " is " + hashValue);
            return hashValue;
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final FileNotFoundException e) {
            // e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (!created) {
                this.logger.warning("Error creating hash of " + element);
            }
        }
        return null;
    }

    /**
     * If the cache is enabled
     * 
     * @param u
     */
    public void setEnabled(final boolean u) {
        this.cacheEnabled = u;
    }

    /**
     * If true, weak caching will be enabled.
     * 
     * @param w
     */
    public void setWeakMode(boolean w) {
        if (w == true) {
            this.logger.fine("Weak mode for caching was enabled.");
        }
        this.weakMode = w;
    }

    /**
     * @param cachePath
     */
    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }
}
