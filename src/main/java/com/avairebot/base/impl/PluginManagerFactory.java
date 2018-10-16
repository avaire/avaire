/*
 * PluginManagerFactory.java
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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avairebot.AvaIre;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.base.Plugin;
import com.avairebot.base.PluginConfiguration;
import com.avairebot.base.BackendPluginManager;

/**
 * Factory class to create new {@link BackendPluginManager}. This is your entry and starting point for
 * using JSPF. Create a new manager by calling one of the enclosed methods.<br/><br/>
 *
 * There should be no need to access this class (or call any of its methods) more than once
 * during the lifetime of your application.
 * 
 * @author Ralf Biedert
 */
public class PluginManagerFactory {

    /** This class will not be instantiated. */
    private PluginManagerFactory() {
        //
    }

    /**
     * Creates a new {@link BackendPluginManager}, no user configuration is used. The manager will
     * be (almost) empty, i.e., containing no {@link Plugin}s except some internal ones.<br/><br/>
     * 
     * The next thing you should probably do is adding your own plugins by calling 
     * <code>addPluginsFrom()</code>.
     * 
     * @return  A fresh plugin manager.
     */
    public static BackendPluginManager createPluginManager(AvaIre bot) {
        return createPluginManager(new Properties(), bot);
    }

    /**
     * Creates a new {@link BackendPluginManager} with a supplied user configuration. The user configuration
     * can be obtained by using the {@link PluginConfiguration}. <br/><br/>
     * 
     * The next thing you should probably do is adding your own plugins by calling 
     * <code>addPluginsFrom()</code>.<br/><br/>
     * 
     * In order to assist debugging, you can set one of {@link JSPFProperties}'s preferences by
     * calling: <code>setProperty(BackendPluginManager.class, "logging.level", "INFO")</code> (INFO
     * can be replaced by OFF, WARNING, INFO, FINE, FINER or FINEST respectively.
     * 
     * @param initialProperties Initial properties to use.
     * 
     * @return A fresh manager with the supplied configuration. 
     */
    public static BackendPluginManager createPluginManager(final Properties initialProperties, AvaIre bot) {

        // Setup logging
        if (initialProperties.containsKey("com.avairebot.base.BackendPluginManager.logging.level")) {
            final String level = initialProperties.getProperty("com.avairebot.base.BackendPluginManager.logging.level");

            setLogLevel(Level.parse(level));
        }

        // Lower the JMDNS level (TODO: Why doen't this work?) 
        Logger.getLogger("javax.jmdns").setLevel(Level.OFF);

        return new PluginManagerImpl(initialProperties, bot);
    }

    /**
     * Sets logging to the specified level. 
     */
    private static void setLogLevel(Level level) {
        Logger.getLogger("").setLevel(level);

        Handler[] handlers = Logger.getLogger("").getHandlers();

        for (Handler handler : handlers) {
            handler.setLevel(level);
        }
    }
}
