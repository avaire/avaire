/*
 * PluginUtil.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.avairebot.base.Plugin;
import com.avairebot.base.Plugin;

/**
 * A set of inspection methods for an existing plugin. Should only be required 
 * and used internally. 
 * 
 * @author Ralf Biedert
 */
public class PluginUtil {
    final Plugin plugin;

    /**
     * The plugin to wrap.
     * 
     * @param plugin
     */
    public PluginUtil(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lists all primary interfaces.
     * 
     * @return A list of primary interfaces.>
     */
    @SuppressWarnings({ "unchecked" })
    public Collection<Class<? extends Plugin>> getPrimaryInterfaces() {
        final Collection<Class<? extends Plugin>> rval = getPluginInterfaces();
        List<Class<?>> candidates = new ArrayList<Class<?>>();

        // Filter all redundant interfaces
        int lastSize = 0;
        do {
            lastSize = rval.size();

            // Create a copy of all our plugins
            candidates = new ArrayList<Class<?>>(rval);

            rval.clear();

            // For every plugin ...
            for (Class<?> class2 : candidates) {
                // ... compare it with every other ...
                boolean hasSuper = false;

                for (Class<?> class3 : candidates) {
                    // ... and see if it can be assigned from another one that is not the same, if that
                    // is the case, it's useless, if not, we keep it.
                    if (class2.isAssignableFrom(class3) && class2 != class3) {
                        hasSuper = true;
                    }
                }

                if (!hasSuper && !rval.contains(class2))
                    rval.add((Class<? extends Plugin>) class2);
            }
        } while (lastSize != rval.size());

        return rval;
    }

    /**
     * Lists all toplevel plugin interfaces
     * 
     * @return The list of all plugin interfaces.
     */
    @SuppressWarnings({ "unchecked" })
    public Collection<Class<? extends Plugin>> getPluginInterfaces() {
        final Collection<Class<? extends Plugin>> rval = new ArrayList<Class<? extends Plugin>>();

        List<Class<?>> candidates = new ArrayList<Class<?>>();
        Class<?> current = this.plugin.getClass();

        // Check the plugin class and all its parents what interfaces they contain 
        while (current != null && !Object.class.equals(current)) {
            final List<Class<?>> c = Arrays.asList(current.getInterfaces());

            for (Class<?> class1 : c) {
                if (candidates.contains(class1)) continue;
                candidates.add(class1);
            }

            current = current.getSuperclass();
        }

        // Filter first round (only plugin interfaces should be left)
        for (Class<?> class1 : candidates) {
            if (!Plugin.class.isAssignableFrom(class1)) continue;

            rval.add((Class<? extends Plugin>) class1);
        }

        return rval;
    }

    /**
     * Lists all plugin interfaces.
     * 
     * @return Lists all primary interfaces.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<Class<? extends Plugin>> getAllPluginInterfaces() {
        final Collection<Class<? extends Plugin>> pluginInterfaces = getPluginInterfaces();
        final Collection<Class<? extends Plugin>> rval = new ArrayList<Class<? extends Plugin>>();

        final ArrayList<Class<? extends Plugin>> x = new ArrayList<Class<? extends Plugin>>(pluginInterfaces);

        // As long as there are new classes (or ones we havent seen yet)
        while (x.size() > 0) {
            Class c = x.get(0);
            x.remove(0);

            // And walk up that class.
            if (!rval.contains(c)) {
                rval.add(c);
            }

            // Get all super interfaces 
            Class[] interfaces = c.getInterfaces();
            for (Class class1 : interfaces) {
                if (x.contains(class1)) continue;
                x.add(class1);
            }
        }

        return rval;
    }
}
