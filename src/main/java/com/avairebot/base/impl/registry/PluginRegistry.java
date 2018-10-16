/*
 * PluginRegistry.java
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
package com.avairebot.base.impl.registry;

import static net.jcores.CoreKeeper.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jcores.interfaces.functions.F1;
import com.avairebot.base.Plugin;
import com.avairebot.base.impl.registry.PluginClassMetaInformation.Dependency;
import com.avairebot.base.impl.registry.PluginClassMetaInformation.PluginClassStatus;

/**
 * The registry keeps track of all instantiated plugins.
 * 
 * @author Ralf Biedert
 */
public class PluginRegistry {

    /** Stores meta information related to a plugin */
    private final Map<Plugin, PluginMetaInformation> pluginMetaInformation;

    /** Stores meta information related to a plugin class */
    private final Map<Class<? extends Plugin>, PluginClassMetaInformation> pluginClassMetaInformation;

    /**
     * Creates a new registry
     */
    public PluginRegistry() {
        this.pluginMetaInformation = new ConcurrentHashMap<Plugin, PluginMetaInformation>();
        this.pluginClassMetaInformation = new ConcurrentHashMap<Class<? extends Plugin>, PluginClassMetaInformation>();
    }

    /**
     * Returns all plugins, regardless of their status
     * 
     * @return .
     */
    public Collection<Plugin> getAllPlugins() {
        return this.pluginMetaInformation.keySet();
    }

    /**
     * Returns the metainfromation of a plugin
     * 
     * @param plugin
     * @return .
     */
    public PluginMetaInformation getMetaInformationFor(Plugin plugin) {
        return this.pluginMetaInformation.get(plugin);
    }

    /**
     * Returns the metainfromation of a pluginclass
     * 
     * @param clazz
     * @return .
     */
    public PluginClassMetaInformation getMetaInformationFor(Class<? extends Plugin> clazz) {
        return this.pluginClassMetaInformation.get(clazz);
    }

    /**
     * Remove all plugin references
     */
    public void clear() {
        this.pluginClassMetaInformation.clear();
        this.pluginMetaInformation.clear();
    }

    /**
     * Registers a plugin with the given meta information
     * 
     * @param plugin
     * @param metaInformation
     */
    public void registerPlugin(Plugin plugin, PluginMetaInformation metaInformation) {
        this.pluginMetaInformation.put(plugin, metaInformation);
    }

    /**
     * @param c
     * @param metaInformation
     */
    public void registerPluginClass(Class<? extends Plugin> c,
                                    PluginClassMetaInformation metaInformation) {
        this.pluginClassMetaInformation.put(c, metaInformation);
    }

    /**
     * Returns all classes with a given status.
     * 
     * @param status
     * @return .
     */
    public Collection<Class<? extends Plugin>> getPluginClassesWithStatus(PluginClassMetaInformation.PluginClassStatus status) {
        final List<Class<? extends Plugin>> rval = new ArrayList<Class<? extends Plugin>>();
        final Set<Class<? extends Plugin>> keySet = this.pluginClassMetaInformation.keySet();

        for (Class<? extends Plugin> class1 : keySet) {
            final PluginClassMetaInformation metaInformation = this.pluginClassMetaInformation.get(class1);

            if (metaInformation.pluginClassStatus == status) rval.add(class1);
        }

        return rval;
    }

    /**
     * Prints a report of this registry.
     */
    public void report() {
        System.out.println();
        System.out.println(">>> Class Report <<<");
        final Set<Class<? extends Plugin>> keySet = this.pluginClassMetaInformation.keySet();
        for (Class<? extends Plugin> class1 : keySet) {
            final PluginClassMetaInformation meta = this.pluginClassMetaInformation.get(class1);
            System.out.print("    " + class1.getCanonicalName() + " (status:'" + meta.pluginClassStatus);
            System.out.print("'; dependencies:'" + $(meta.dependencies).map(new F1<PluginClassMetaInformation.Dependency, String>() {
                public String f(PluginClassMetaInformation.Dependency x) {
                    return x.pluginClass.getSimpleName();
                }
            }).string().join(","));
            System.out.print("'; origin:'" + meta.pluginOrigin + "';)");
            System.out.println();
        }

        System.out.println();
        System.out.println(">>> Object Report <<<");
        final Set<Plugin> keySet2 = this.pluginMetaInformation.keySet();
        for (Plugin plugin : keySet2) {
            final PluginMetaInformation meta = this.pluginMetaInformation.get(plugin);
            System.out.println("    " + plugin + " (status:'" + meta.pluginStatus + "')");
        }

        System.out.println();
    }
}
