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
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.plugin.Plugin;
import com.avairebot.database.collection.DataRow;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import com.avairebot.plugin.translators.PluginHolderTranslator;
import com.avairebot.plugin.translators.PluginLoaderTranslator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class PluginManager {

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private final File pluginsFolder;

    /**
     * The AvaIre class instance, this is used to access
     * and interact with the rest of the application.
     */
    private final AvaIre avaire;

    private final Set<PluginLoader> plugins = new HashSet<>();

    /**
     * Creates a new plugin manager instances, this will create
     * the plugins directory that all the plugins should be
     * loaded from if it doesn't already exists.
     */
    public PluginManager(AvaIre avaire) {
        this.avaire = avaire;

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
    public void loadPlugins(AvaIre avaire) throws InvalidPluginsPathException, InvalidPluginException {
        if (!pluginsFolder.isDirectory() || pluginsFolder.listFiles() == null) {
            throw new InvalidPluginsPathException("Invalid plugins path exception, the plugins path is not a directory.");
        }

        List<String> pluginsToDelete = new ArrayList<>();
        Object deletedPlugins = avaire.getCache().getAdapter(CacheType.FILE).get("deleted-plugins");
        if (deletedPlugins instanceof List) {
            pluginsToDelete.addAll((List<String>) deletedPlugins);
        }

        //noinspection ConstantConditions
        for (File file : pluginsFolder.listFiles()) {
            if (pluginsToDelete.contains(file.toString())) {
                file.delete();
                continue;
            }
            loadPlugin(file);
        }

        avaire.getCache().getAdapter(CacheType.FILE).forget("deleted-plugins");
    }

    /**
     * Loads all the plugins from the plugin index, this will fetch all the
     * plugins registered with the plugin index, then check if the plugin
     * is already installed, if it's not install it will re-download
     * the indexed version and enable the plugin.
     *
     * @param avaire The AvaIre application class instance.
     */
    public void loadPluginsFromIndex(AvaIre avaire) {
        boolean shouldMigrate = false;

        try {
            for (DataRow row : avaire.getDatabase().newQueryBuilder(Constants.INSTALLED_PLUGINS_TABLE_NAME).get()) {
                Plugin plugin = getPluginByName(row.getString("name"));
                if (plugin == null) {
                    log.warn("Found no suitable plugin by name for {} from the plugin index!", row.getString("name"));
                    continue;
                }

                if (plugin.isInstalled()) {
                    log.debug("{} from the plugin index is already installed, skipping index.", plugin.getName());
                    continue;
                }

                String downloadUrl = row.getString("download_url");
                log.info("Re-downloading {} {} from {} using download resource {}",
                    plugin.getName(), row.getString("version"), row.getString("repository"), downloadUrl
                );

                final File pluginFile = new File(
                    avaire.getPluginManager().getPluginsFolder(),
                    plugin.getName() + ".jar"
                );

                try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(pluginFile)) {

                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    log.error("Failed to download the {} resource, error: {}", plugin.getName(), e.getMessage(), e);
                    continue;
                }

                try {
                    avaire.getPluginManager()
                        .loadPlugin(pluginFile)
                        .invokePlugin(avaire);
                } catch (InvalidPluginsPathException | InvalidPluginException e) {
                    log.error("Failed to invoke the {} plugin, error: {}", plugin.getName(), e.getMessage(), e);
                }

                shouldMigrate = true;
            }
        } catch (SQLException e) {
            log.error("Failed to fetch plugin index from the database, error: {}", e.getMessage(), e);
        }

        if (shouldMigrate) {
            try {
                avaire.getDatabase().getMigrations().up();
            } catch (SQLException e) {
                log.error("Failed to migrate the database after enabling plugins, error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Attempts to load the plugin from disk using the given file.
     *
     * @param file The file that should be attempted to be loaded as a plugin.
     * @return The plugin loader associated with the loaded plugin,
     * or {@code NULL} if an exception were thrown.
     * @throws InvalidPluginsPathException This is thrown if the plugins directory doesn't exist, or
     *                                     the bot doesn't have read access to the directory.
     * @throws InvalidPluginException      This is thrown if the plugin is not valid in some way.
     */
    public PluginLoader loadPlugin(File file) throws InvalidPluginsPathException, InvalidPluginException {
        if (file.isDirectory() || file.isHidden()) {
            return null;
        }

        try {
            log.debug("Attempting to load plugin: " + file.toString());
            PluginLoader pluginLoader = new PluginLoader(file, pluginsFolder);

            plugins.add(pluginLoader);

            return pluginLoader;
        } catch (IOException e) {
            log.error("Failed to load the {} plugin, error: {}", file.getName(), e.getMessage(), e);

            return null;
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

    /**
     * Gets the plugin data folder, this is the directory that the plugins personal
     * config and any data related to the plugin will be stored in.
     *
     * @return The plugin data folder.
     */
    public File getPluginsFolder() {
        return pluginsFolder;
    }

    /**
     * Unloads the given plugin, removing it from the plugin registrar, disabling it in the process.
     *
     * @param plugin The plugin that should be unloaded.
     * @return {@code True} if the plugin was unloaded successfully.
     */
    public boolean unloadPlugin(Plugin plugin) {
        Iterator<PluginLoader> iterator = plugins.iterator();

        while (iterator.hasNext()) {
            PluginLoader next = iterator.next();

            if (!next.getName().equalsIgnoreCase(plugin.getName())) {
                continue;
            }

            next.unregisterPlugin(AvaIre.getInstance());
            iterator.remove();

            return true;
        }

        return false;
    }

    /**
     * Gets a list of the plugin holders for the official plugins, this
     * represents the list of plugins registered on the plugins.json
     * file on the public avaire/plugin GitHub repository.
     *
     * @return A list of official plugins approved behind the team of Ava.
     */
    @SuppressWarnings("unchecked")
    public List<PluginHolder> getOfficialPluginsList() {
        Object plugins = AvaIre.getInstance().getCache().getAdapter(CacheType.MEMORY).remember("plugins", 10800, () -> {
            try {
                Connection.Response execute = Jsoup.connect("https://raw.githubusercontent.com/avaire/plugins/master/plugins.json")
                    .ignoreContentType(true)
                    .execute();

                JSONObject obj = new JSONObject(execute.body());
                JSONArray data = obj.getJSONArray("data");

                List<PluginHolder> pluginList = new ArrayList<>();
                for (Object aData : data) {
                    pluginList.add(new PluginHolder((JSONObject) aData));
                }

                return pluginList;
            } catch (IOException e) {
                log.error("Failed to fetch plugins from github: " + e.getMessage(), e);

                return null;
            }
        });

        if (!(plugins instanceof List)) {
            return null;
        }
        return (List<PluginHolder>) plugins;
    }

    /**
     * Gets the plugin instance for the plugin with the given name.
     *
     * @param name The name of the plugin that should be returned.
     * @return The plugin translator instance for the matching plugin with
     * the given name, or {@code NULL} if there were no match.
     */
    public final Plugin getPluginByName(String name) {
        List<PluginHolder> pluginHolders = avaire.getPluginManager().getOfficialPluginsList();
        if (pluginHolders == null) {
            return null;
        }

        for (PluginLoader pluginLoader : avaire.getPluginManager().getPlugins()) {
            if (pluginLoader.getName().equalsIgnoreCase(name)) {
                return new PluginLoaderTranslator(pluginLoader, avaire.getPluginManager().getOfficialPluginsList());
            }
        }

        for (PluginHolder holder : pluginHolders) {
            if (holder.getName().equalsIgnoreCase(name)) {
                return new PluginHolderTranslator(holder);
            }
        }

        return null;
    }
}
