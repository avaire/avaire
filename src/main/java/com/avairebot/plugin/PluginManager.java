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

import com.avairebot.AvaIre;
import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class PluginManager {

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private final File pluginsFolder;

    private final Set<PluginLoader> plugins = new HashSet<>();

    public PluginManager(AvaIre bot) {
        this.pluginsFolder = new File("plugins");

        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }
        final JSPFProperties props = new JSPFProperties(); // Lets set up some properties so it's less sloppy looking
        props.setProperty(PluginManager.class, "cache.enabled", "false");
        props.setProperty(PluginManager.class, "supervision.enabled", "true");
        pm = PluginManagerFactory.createPluginManager(props, bot);
    }

    public void loadPlugins() throws InvalidPluginsPathException, InvalidPluginException {
        if (!pluginsFolder.isDirectory() || pluginsFolder.listFiles() == null) {
            throw new InvalidPluginsPathException("Invalid plugins path exception, the plugins path is not a directory.");
        }
        pm.addPluginsFrom(new File("plugins/").toURI()); // One line does it all
    }

    private BackendPluginManager pm;

    public BackendPluginManager get() {
        return pm;
    }

    public Set<PluginLoader> getPlugins() {
        return plugins;
    }
}
