/*
 * BusImpl.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
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
package com.avairebot.base.util;

import java.util.ArrayList;
import java.util.Collection;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.options.getplugin.OptionPluginSelector;
import com.avairebot.base.options.getplugin.PluginSelector;
import com.avairebot.base.Plugin;

/**
 * Helper functions for {@link BackendPluginManager} interface. The util uses the embedded
 * interface to provide more convenience features.   
 *
 * @author Ralf Biedert
 * @see BackendPluginManager
 */
public class PluginManagerUtil {

    private final BackendPluginManager backendPluginManager;

    /**
     * Creates a new util for the given interface.
     * 
     * @param pm The interface to create the utils for.
     */
    public PluginManagerUtil(BackendPluginManager pm) {
        this.backendPluginManager = pm;
    }

    /**
     * Returns all interfaces implementing the given interface, not just the first, 
     * 'random' match. Use this method if you want to list  the registed plugins (or 
     * select from them on your own). For example, to get all plugins implementing the 
     * <code>Chat</code> interface, write:<br/><br/>
     * 
     * <code>
     * getPlugins(Chat.class);
     * </code>
     * 
     * @param <P> Type of the requested plugin.
     * @param plugin The interface to request.
     * @see OptionPluginSelector
     * @return A collection of all plugins implementing the given interface.
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin) {
        return getPlugins(plugin, new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                return true;
            }
        });
    }

    /**
     * Returns all interfaces implementing the given interface AND satisfying the 
     * given plugin selector. Use this method if you want to list some of the 
     * registed plugins (or select from them on your own). 
     * 
     * @param <P> Type of the requested plugin.
     * @param plugin The interface to request. 
     * @param selector The selector will be called for each available plugin. When 
     * it returns <code>true</code> the plugin will be added to the return value.
     * @see OptionPluginSelector  
     * @return A collection of plugins for which the collector return true.
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin,
                                                       final PluginSelector<P> selector) {
        final Collection<P> allPlugins = new ArrayList<P>();

        this.backendPluginManager.getPlugin(plugin, new OptionPluginSelector<P>(new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                if (selector.selectPlugin(p)) {
                    allPlugins.add(p);
                }
                return false;
            }
        }));

        return allPlugins;
    }

}
