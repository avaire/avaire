package com.avairebot.plugin;

import com.avairebot.AvaIre;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    public PluginManager() {
        this.pluginsFolder = new File("plugins");

        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }
    }

    public void loadPlugins() throws InvalidPluginsPathException, InvalidPluginException {
        if (!pluginsFolder.isDirectory() || pluginsFolder.listFiles() == null) {
            throw new InvalidPluginsPathException("Invalid plugins path exception, the plugins path is not a directory.");
        }

        //noinspection ConstantConditions
        for (File file : pluginsFolder.listFiles()) {
            if (file.isDirectory() || file.isHidden()) continue;

            try {
                log.debug(String.format("Attempting to load plugin: %s", file));
                PluginLoader pluginLoader = new PluginLoader(file, pluginsFolder);

                plugins.add(pluginLoader);
            } catch (IOException ex) {
                AvaIre.getLogger().error("IOException on PluginManager.loadPlugins", ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    public Set<PluginLoader> getPlugins() {
        return plugins;
    }
}
