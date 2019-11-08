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
import com.avairebot.exceptions.InvalidPluginsPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PluginManager {

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private final File pluginsFolder;

    private final Set<PluginLoader> plugins = new HashSet<>();

    /**
     * Creates a new plugin manager instances, this will create
     * the plugins directory that all the plugins should be
     * loaded from if it doesn't already exists.
     */
    public PluginManager() {
        this.pluginsFolder = new File("plugins");

        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }
    }

    /**
     * Loads all the plugins in the plugins directory.
     *
     * @throws InvalidPluginsPathException This is thrown if the plugins directory doesn't exist, or
     *                                     the bot doesn't have read access to the directory.
     * @throws InvalidPluginException      This is thrown if the plugin is not valid in some way.
     */
    public void loadPlugins() throws InvalidPluginsPathException, InvalidPluginException {
        if (!pluginsFolder.isDirectory() || pluginsFolder.listFiles() == null) {
            throw new InvalidPluginsPathException("Invalid plugins path exception, the plugins path is not a directory.");
        }

        //noinspection ConstantConditions
        for (File file : pluginsFolder.listFiles()) {
            if (file.isDirectory() || file.isHidden()) continue;

            try {
                log.debug("Attempting to load plugin: " + file.toString());
                PluginLoader pluginLoader = new PluginLoader(file, pluginsFolder);

                plugins.add(pluginLoader);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gets a set of loaded plugins.
     *
     * @return A set of loaded plugins.
     */
    public Set<PluginLoader> getPlugins() {
        return plugins;
    }
}
