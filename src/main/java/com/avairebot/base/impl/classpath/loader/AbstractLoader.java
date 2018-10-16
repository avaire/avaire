/*
 * AbstractLoader.java
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.configuration.ConfigurationFile;
import com.avairebot.base.annotations.configuration.IsDisabled;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.options.getplugin.OptionCapabilities;
import com.avairebot.base.util.PluginConfigurationUtil;
import com.avairebot.base.Plugin;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.configuration.ConfigurationFile;
import com.avairebot.base.annotations.configuration.IsDisabled;
import com.avairebot.base.impl.PluginManagerImpl;
import com.avairebot.base.impl.classpath.ClassPathManager;
import com.avairebot.base.impl.classpath.locator.AbstractClassPathLocation;
import com.avairebot.base.impl.registry.PluginClassMetaInformation;
import com.avairebot.base.impl.registry.PluginClassMetaInformation.Dependency;
import com.avairebot.base.impl.registry.PluginClassMetaInformation.PluginClassStatus;
import com.avairebot.base.impl.registry.PluginMetaInformation.PluginStatus;
import com.avairebot.base.impl.registry.PluginRegistry;
import com.avairebot.base.impl.spawning.SpawnResult;
import com.avairebot.base.impl.spawning.Spawner;
import com.avairebot.base.options.getplugin.OptionCapabilities;
import com.avairebot.base.util.PluginConfigurationUtil;
import com.avairebot.plugin.JavaPlugin;
import com.avairebot.plugin.PluginLoader;

/**
 * The abstract base class of all loaders, provides methods for spawning classes.
 * 
 * @author Ralf Biedert
 */
public abstract class AbstractLoader {

    /** */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Grants access to various shared variables. */
    protected final PluginManagerImpl pluginManager;

    protected final AvaIre bot;

    /**
     * @param pluginManager
     */
    public AbstractLoader(PluginManagerImpl pluginManager, AvaIre bot) {
        this.pluginManager = pluginManager;
        this.bot = bot;
    }

    /**
     * @param uri
     * @return .
     */
    public abstract boolean handlesURI(URI uri);

    /**
     * Load plugins from a given source
     * 
     * @param uri
     */
    public abstract void loadFrom(URI uri);

    /**
     * Tries to load a class from a given source. If it is a plugin, it will be
     * registered.
     * 
     * @param location
     * @param name
     */
    @SuppressWarnings({ "unchecked", "boxing" })
    protected void tryToLoadClassAsPlugin(AbstractClassPathLocation location,
                                          final String name, AvaIre bot) {
        this.logger.finest("Trying to load " + name + " as a plugin.");

        // Obtain some shared objects
        // final JARCache jarCache = this.backendPluginManager.getJARCache();
        final ClassPathManager classPathManager = this.pluginManager.getClassPathManager();
        final PluginRegistry pluginRegistry = this.pluginManager.getPluginRegistry();
        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.pluginManager.getPluginConfiguration());
        final Spawner spawner = this.pluginManager.getSpawner();

        // Obtain information
        // final JARInformation jarInformation = jarCache.getJARInformation(name);
        // final String file = jarCache.classTofile(name);

        try {
            // Get class of the candidate
            final Class<?> possiblePlugin = classPathManager.loadClass(location, name);

            // Don't load plugins already spawned.
            if (name.startsWith("com.avairebot.base")) return;

            // Get the plugin's annotation
            final PluginImplementation annotation = possiblePlugin.getAnnotation(PluginImplementation.class);

            if(annotation.APIversion().equalsIgnoreCase(Constants.APIVersion) && !annotation.APIversion().equalsIgnoreCase("SYSTEM"))
            {
                this.logger.info("Skipping plugin " + possiblePlugin + " because it's not for this version of Avaire!");
                return;
            }

            // Nothing to load here if no annotation is present
            if (annotation == null) { return; }

            // Don't load classes already loaded from this location
            final PluginClassMetaInformation preexistingMeta = pluginRegistry.getMetaInformationFor((Class<? extends Plugin>) possiblePlugin);
            if (preexistingMeta != null) {
                this.logger.info("Skipping plugin " + possiblePlugin + " because we already have it ");
                return;
            }

            // Register class at registry
            final PluginClassMetaInformation metaInformation = new PluginClassMetaInformation();
            metaInformation.pluginClassStatus = PluginClassStatus.ACCEPTED;
            if (location != null) {
                metaInformation.pluginOrigin = location.getToplevelLocation();
            } else {
                metaInformation.pluginOrigin = new URI("classpath://UNDEFINED");
            }
            pluginRegistry.registerPluginClass((Class<? extends Plugin>) possiblePlugin, metaInformation);

            // Update the class information of the corresponding cache entry
            this.logger.finer("Updating cache information");

            // Avoid loading if annotation request it.
            if (pcu.getBoolean(possiblePlugin, "plugin.disabled", false) || possiblePlugin.getAnnotation(IsDisabled.class) != null) {
                metaInformation.pluginClassStatus = PluginClassStatus.DISABLED;
                this.logger.fine("Ignoring " + name + " due to request.");
                return;
            }

            // Up from here we know we will (eventually) use the plugin. So load its
            // configuration.
            final String properties = (possiblePlugin.getAnnotation(ConfigurationFile.class) != null) ? possiblePlugin.getAnnotation(ConfigurationFile.class).file() : null;
            if (properties != null && properties.length() > 0) {
                final String resourcePath = name.replaceAll("\\.", "/").replaceAll(possiblePlugin.getSimpleName(), "") + properties;
                this.logger.fine("Adding configuration from " + resourcePath + " for plugin " + name);

                final Properties p = new Properties();

                // Try to load resource by special classloader
                try {
                    p.load(classPathManager.getResourceAsStream(location, resourcePath));

                    final Set<Object> keys = p.keySet();

                    // Add every string that is not already in the configuration.
                    for (final Object object : keys) {
                        if (pcu.getString(null, (String) object) != null) {
                            this.pluginManager.getPluginConfiguration().setConfiguration(null, (String) object, p.getProperty((String) object));
                        }
                    }
                } catch (final IOException e) {
                    this.logger.warning("Unable to load properties " + resourcePath + " although requested");
                } catch (final NullPointerException e) {
                    this.logger.warning("Unable to load properties " + resourcePath + " although requested. Probably not in package.");
                }
            }

            // Obtain dependencies
            metaInformation.dependencies = spawner.getDependencies((Class<? extends Plugin>) possiblePlugin);

            // If the class has unfulfilled dependencies, add it to our list.
            if (metaInformation.dependencies.size() == 0) {
                metaInformation.pluginClassStatus = PluginClassStatus.SPAWNABLE;
            } else {
                metaInformation.pluginClassStatus = PluginClassStatus.CONTAINS_UNRESOLVED_DEPENDENCIES;
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            this.logger.warning("ClassNotFoundException. Unable to inspect class " + name + " although it appears to be one.");
        } catch (final NoClassDefFoundError e) {
            e.printStackTrace();
            this.logger.finer("Ignored class " + name + " due to unresolved dependencies");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // We always try to load pending classes ...
        processPending();

        try {
            AvaIre.getLogger().debug("Attempting to load plugin: " + name);
            PluginLoader pluginLoader = new PluginLoader(new File(location.getToplevelLocation()), new File("plugins"));
            bot.getPluginManager().getPlugins().add(pluginLoader);
        } catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Try to load a pending class.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void processPending() {

        // Obtain shared objects
        final PluginRegistry pluginRegistry = this.pluginManager.getPluginRegistry();
        final Spawner spawner = this.pluginManager.getSpawner();

        // All classes we want to spawn
        final Collection<Class<? extends Plugin>> toSpawn = new ArrayList<Class<? extends Plugin>>();
        toSpawn.addAll(pluginRegistry.getPluginClassesWithStatus(PluginClassStatus.CONTAINS_UNRESOLVED_DEPENDENCIES));
        toSpawn.addAll(pluginRegistry.getPluginClassesWithStatus(PluginClassStatus.SPAWNABLE));

        // Check if there is work to do.
        if (toSpawn.size() == 0) return;

        boolean loopAgain;

        do {
            // Classes we want to spawn
            final Collection<Class<? extends Plugin>> spawned = new ArrayList<Class<? extends Plugin>>();

            // Reset hasLoaded flag
            loopAgain = false;

            // Check all known classes ...
            for (final Class c : toSpawn) {
                this.logger.fine("Trying to load pending " + c);

                final PluginClassMetaInformation metaInformation = pluginRegistry.getMetaInformationFor(c);

                // If the class is spawnable, spawn it ...
                if (metaInformation.pluginClassStatus == PluginClassStatus.SPAWNABLE) {
                    this.logger.fine("Class found as SPAWNABLE. Trying to spawn it now " + c);

                    //
                    // The magic line: spawn it.
                    //
                    final SpawnResult p = spawner.spawnPlugin(c);

                    // In case we were successful ...
                    if (p != null && p.metaInformation.pluginStatus != PluginStatus.FAILED) {

                        // Link the parent class meta information
                        p.metaInformation.classMeta = metaInformation;

                        // Check if the class is active or only lazy spawned
                        if (p.metaInformation.pluginStatus == PluginStatus.ACTIVE) {
                            // Mark the class a spawned
                            metaInformation.pluginClassStatus = PluginClassStatus.SPAWNED;
                            spawned.add(c);

                            this.pluginManager.hookPlugin(p);
                        }

                        // Lazy spawn ...
                        if (p.metaInformation.pluginStatus == PluginStatus.SPAWNED) {
                            metaInformation.pluginClassStatus = PluginClassStatus.LAZY_SPAWNED;
                            throw new IllegalStateException("Lazy spawning not supported yet!");
                        }

                        // And loop once more.
                        loopAgain = true;
                        break;
                    }

                    // This case is bad ...
                    this.logger.warning("Failed to spawn class  " + c);
                    metaInformation.pluginClassStatus = PluginClassStatus.FAILED;
                }

                // Check if we can switch the class to SPAWNABLE
                if (metaInformation.pluginClassStatus == PluginClassStatus.CONTAINS_UNRESOLVED_DEPENDENCIES) {
                    this.logger.fine("Trying to solve dependencies for class " + c);
                    boolean resolvedAll = true;

                    // Check all dependencies
                    for (Dependency d : metaInformation.dependencies) {
                        if (d.isOptional) {
                            this.logger.finest("Skipping dependency as optional " + d.pluginClass);
                            continue;
                        }

                        if (this.pluginManager.getPlugin(d.pluginClass, new OptionCapabilities(d.capabilites)) == null) {
                            resolvedAll = false;
                        }

                    }

                    // Nice. So this class will be spawned in one of the next rounds ...
                    if (resolvedAll) {
                        metaInformation.pluginClassStatus = PluginClassStatus.SPAWNABLE;
                        loopAgain = true;
                        break;
                    }
                }
            }

            // Remove all spawned from the list (not really necessary, is it?)
            toSpawn.removeAll(spawned);

        } while (loopAgain && toSpawn.size() > 0);
    }
}
