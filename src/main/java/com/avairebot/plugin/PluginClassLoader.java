/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.plugin;

import com.avairebot.exceptions.InvalidPluginException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassLoader extends URLClassLoader {

    private final JavaPlugin plugin;

    private final PluginLoader loader;
    private final File dataFolder;
    private final File file;

    private final ConcurrentHashMap classes = new ConcurrentHashMap();

    PluginClassLoader(PluginLoader loader, ClassLoader parent, File dataFolder, File file) throws InvalidPluginException, MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);

        this.loader = loader;
        this.dataFolder = dataFolder;
        this.file = file;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(loader.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class `" + loader.getMain() + "'", ex);
            }

            Class<?> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("main class `" + loader.getMain() + "' does not extend JavaPlugin", ex);
            }

            this.plugin = ((JavaPlugin) pluginClass.getConstructor().newInstance());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InvalidPluginException("No public constructor", e);
        } catch (InstantiationException e) {
            throw new InvalidPluginException("Abnormal plugin type", e);
        } catch (InvocationTargetException e) {
            throw new InvalidPluginException("Failed to invoke the plugin constructor", e);
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public PluginLoader getLoader() {
        return loader;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getFile() {
        return file;
    }

    public Map<String, Class<?>> getClasses() {
        return classes;
    }
}
