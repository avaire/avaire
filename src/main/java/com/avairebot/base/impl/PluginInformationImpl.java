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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.annotations.Capabilities;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.annotations.meta.Stateless;
import com.avairebot.base.annotations.meta.Version;
import com.avairebot.base.impl.registry.PluginMetaInformation;
import com.avairebot.base.impl.registry.PluginRegistry;
import com.avairebot.base.Plugin;
import com.avairebot.base.PluginInformation;

/**
 * TODO: Make plugin threadsafe
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version(version = 1 * Version.UNIT_MAJOR)
@Stateless
@PluginImplementation(APIversion = "SYSTEM")
public class PluginInformationImpl implements PluginInformation {
    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**  */
    @InjectPlugin
    public BackendPluginManager backendPluginManager;

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.base.PluginInformation#getInformation(com.avairebot.base.
     * PluginInformation.Information, com.avairebot.base.Plugin)
     */
    public Collection<String> getInformation(final Information item, final Plugin plugin) {

        // Needed to query some special information
        final PluginManagerImpl pmi = (PluginManagerImpl) this.backendPluginManager;

        // Prepare return values ...
        final Collection<String> rval = new ArrayList<String>();

        switch (item) {

        case CAPABILITIES:
            // Caps are only supported for plugins currently
            final String[] caps = getCaps(plugin);
            for (final String string : caps) {
                rval.add(string);
            }
            break;

        case AUTHORS:
            Author author = plugin.getClass().getAnnotation(Author.class);
            if (author == null) break;
            rval.add(author.name());
            break;

        case VERSION:
            Version version = plugin.getClass().getAnnotation(Version.class);
            if (version == null) break;
            rval.add(Integer.toString(version.version()));
            break;

        case CLASSPATH_ORIGIN:
            final PluginRegistry pluginRegistry = pmi.getPluginRegistry();
            final PluginMetaInformation metaInformation = pluginRegistry.getMetaInformationFor(plugin);
            if (metaInformation != null && metaInformation.classMeta != null && metaInformation.classMeta.pluginOrigin != null)
                    rval.add(metaInformation.classMeta.pluginOrigin.toString());
            break;

        default:
            this.logger.info("Requested InformationItem is now known!");
            break;
        }

        return rval;
    }

    /**
     * @param plugin
     * @return
     */
    private String[] getCaps(final Plugin plugin) {
        final Class<? extends Plugin> spawnClass = plugin.getClass();

        final Method[] methods = spawnClass.getMethods();

        // Search for proper method
        for (final Method method : methods) {

            // Init methods will be marked by the corresponding annotation.
            final Capabilities caps = method.getAnnotation(Capabilities.class);
            if (caps != null) {

                Object result = null;
                try {
                    result = method.invoke(plugin, new Object[0]);
                } catch (final IllegalArgumentException e) {
                    //
                } catch (final IllegalAccessException e) {
                    //
                } catch (final InvocationTargetException e) {
                    //
                }
                if (result != null && result instanceof String[])
                    return (String[]) result;
            }
        }

        return new String[0];
    }
}
