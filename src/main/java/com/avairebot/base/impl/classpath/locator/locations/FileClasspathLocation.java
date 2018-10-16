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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.classpath.cache.JARCache;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;

/**
 * @author rb
 *
 */
public class FileClasspathLocation extends AbstractClassPathLocation {

    /**
     * @param cache
     * @param realm
     * @param location
     */
    public FileClasspathLocation(JARCache cache, String realm, URI location) {
        super(cache, realm, location);
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#getInputStream(java.lang.String)
     */
    @Override
    public InputStream getInputStream(String entry) {
        final File toplevel = new File(this.location);

        try {
            return new FileInputStream(new File(toplevel.getAbsolutePath() + "/" + entry));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#getType()
     */
    @Override
    public LocationType getType() {
        return LocationType.DIRECTORY;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#listAllEntries()
     */
    @Override
    public Collection<String> listAllEntries() {
        final Collection<String> rval = new ArrayList<String>();
        final File toplevel = new File(this.location);

        try {
            List<File> fileListing = getFileListing(toplevel, new ArrayList<String>());
            for (File file : fileListing) {
                // Only accept true files
                if (file.isDirectory()) continue;

                final String path = Pattern.quote(toplevel.getAbsolutePath());

                String name = "";
                name = file.getAbsolutePath().replaceAll(path, "");
                name = name.substring(1);
                name = name.replace("\\", "/");

                rval.add(name);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return rval;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation#listToplevelClassNames()
     */
    @Override
    public Collection<String> listToplevelClassNames() {
        final Collection<String> rval = new ArrayList<String>();
        final File toplevel = new File(this.location);

        try {
            List<File> fileListing = getFileListing(toplevel, new ArrayList<String>());
            for (File file : fileListing) {
                // Only accept class files
                if (!file.getAbsolutePath().endsWith(".class")) continue;
                //if (file.getAbsolutePath().contains("$")) continue; // Why are we ignoring files with a $ again?

                final String path = Pattern.quote(toplevel.getAbsolutePath());

                String name = "";
                name = file.getAbsolutePath().replaceAll(path, "");
                name = name.substring(1);
                name = name.replace("\\", "/");
                name = name.replace("/", ".");

                // Remove trailing .class 
                if (name.endsWith("class")) {
                    name = name.substring(0, name.length() - 6);
                }

                rval.add(name);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return rval;
    }

    /**
     * List all files
     * 
     * @param aStartingDir
     * @return
     * @throws FileNotFoundException
     */
    private List<File> getFileListing(File aStartingDir, List<String> visited)
                                                                              throws FileNotFoundException {
        // Sanity check
        if (aStartingDir == null || aStartingDir.listFiles() == null)
            return new ArrayList<File>();

        this.logger.fine("Obtaining file listing for: " + aStartingDir);

        final String[] ignoreList = new String[] { "/dev/", "/sys/", "/proc/" };
        final List<File> result = new ArrayList<File>();
        for (File file : aStartingDir.listFiles()) {

            boolean skip = false;

            // Very ugly Android related hack, remove this if we have a better solution to detect a recursion.
            for (String ignore : ignoreList) {
                if (file.getAbsolutePath().contains(ignore)) {
                    this.logger.warning("Android hack actve. Ignoring directory containing " + ignore);
                    skip = true;
                }
            }

            // Now skip
            if (skip) continue;

            result.add(file);

            // TODO: Is it possible that this doen't work as expected? Check if there is a better
            // recursion test ...
            {
                String canonical = null;

                // Why can this fail?
                try {
                    canonical = file.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Anyway, safteycheck: Only process this path if it hasn't already been seen
                if (canonical != null) {
                    if (visited.contains(canonical)) continue;
                    visited.add(canonical);
                }
            }

            // Okay, in here the sub element must have been new, inspect it.
            if (file.isDirectory()) {
                List<File> deeperList = getFileListing(file, visited);
                result.addAll(deeperList);
            }
        }
        return result;
    }

}
