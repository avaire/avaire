/*
 * PluginConfigurationImpl.java
 *
 * Copyright (c) 2007, Ralf Biedert All rights reserved.
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
package com.avairebot.base.impl;

import java.util.Properties;
import java.util.logging.Logger;

import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.annotations.meta.Version;
import com.avairebot.base.PluginConfiguration;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.annotations.meta.Version;

/**
 * 
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@PluginImplementation(APIversion = "SYSTEM")
@Version(version = Version.UNIT_MAJOR)
public class PluginConfigurationImpl implements PluginConfiguration {

    /** Actual properties object we use */
    final Properties configuration;

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * @param initialProperties
     */
    protected PluginConfigurationImpl(final Properties initialProperties) {
        this.configuration = initialProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.PluginConfiguration#getConfiguration(java.lang.Class,
     * java.lang.String)
     */
    public synchronized String getConfiguration(final Class<?> root, final String subkey) {
        final String key = getKey(root, subkey);
        final String value = this.configuration.getProperty(key);

        this.logger.fine("Returning '" + value + "' for " + "'" + key + "'");

        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.PluginConfiguration#setConfiguration(java.lang.Class,
     * java.lang.String, java.lang.String)
     */
    public synchronized void setConfiguration(final Class<?> root, final String subkey,
                                              final String value) {
        final String key = getKey(root, subkey);
        this.logger.fine("Setting '" + value + "' for " + "'" + key + "'");

        this.configuration.setProperty(key, value);
    }

    /**
     * Assemble a key for a given root class and subkey string
     * 
     * @param root Root (may be null)
     * @param subkey (subkey to use)
     * @return The fully assembled key.
     */
    private String getKey(final Class<?> root, final String subkey) {
        String prefix = "";
        if (root != null) {
            prefix = root.getName() + ".";
        }

        this.logger.finer("Assembled key '" + prefix + subkey + "'");

        return prefix + subkey;
    }
}
