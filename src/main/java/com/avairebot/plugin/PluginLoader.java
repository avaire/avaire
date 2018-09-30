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

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class PluginLoader {

    private final File file;
    private final JarFile jarFile;
    private final File dataFolder;
    private final PluginClassLoader classLoader;
    private final YamlConfiguration configuration;

    private final List<String> authors = new ArrayList<>();

    PluginLoader(File file, File dataFolder) throws InvalidPluginException, IOException {
        this.file = file;
        this.dataFolder = dataFolder;

        if (!file.exists()) {
            throw new InvalidPluginException(file.getPath() + " does not exists");
        }

        jarFile = new JarFile(file);

        configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("plugin.yml")));
        checkIfPluginYamlIsValid();

        if (configuration.contains("authors")) {
            authors.addAll(configuration.getStringList("authors"));
        } else if (configuration.contains("author")) {
            authors.add(configuration.getString("author"));
        }

        classLoader = new PluginClassLoader(this, AvaIre.class.getClassLoader(), dataFolder, file);
    }

    /**
     * Gets the name of the plugin.
     *
     * @return The name of the plugin.
     */
    public String getName() {
        return configuration.getString("name");
    }

    /**
     * Gets the full class package path to the main class for the plugin.
     *
     * @return The full class package path to th main class for the plugin.
     */
    public String getMain() {
        return configuration.getString("main");
    }

    /**
     * Gets the version of the plugin.
     *
     * @return The version of the plugin.
     */
    public String getVersion() {
        return configuration.getString("version");
    }

    /**
     * Gets the description of the plugin.
     *
     * @return Possibly-null, the description of the plugin.
     */
    @Nullable
    public String getDescription() {
        return configuration.getString("description");
    }

    /**
     * Gets a list of all the authors for the plugin.
     *
     * @return Possibly-empty list, a list of the authors for the plugin.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Gets the plugin data folder, the folder is created
     * at the application root called "/plugins".
     *
     * @return The data folder object for the plugin.
     */
    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * Invokes the plugin, this will prepare all the needed data and
     * call the {@link JavaPlugin#onEnable() onEnable()} method
     * on the {@link JavaPlugin JavaPlugin} instance.
     *
     * @param avaire The AvaIre class instance.
     */
    public void invokePlugin(AvaIre avaire) {
        classLoader.getPlugin().init(avaire, this);
        classLoader.getPlugin().onEnable();
    }

    /**
     * Gets a set of the event listeners that has been registered by the plugin.
     *
     * @return A set of registered event listeners.
     */
    public Set<ListenerAdapter> getEventListeners() {
        return classLoader.getPlugin().getEventListeners();
    }

    /**
     * Loads the resource from the plugin with the given name.
     *
     * @param resourceName The name of the resource that should be loaded.
     * @return The input stream of the resource.
     * @throws IllegalStateException may be thrown if the jar file has been closed
     * @throws ZipException          if a zip file format error has occurred
     * @throws FileNotFoundException if no files with the given name exists within the jar file
     * @throws IOException           if an I/O error has occurred
     * @throws SecurityException     if any of the jar file entries
     *                               are incorrectly signed.
     */
    public InputStream getResource(String resourceName) throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry(resourceName);
        if (jarEntry == null) {
            throw new FileNotFoundException("No resource found called " + resourceName);
        }
        return jarFile.getInputStream(jarEntry);
    }

    /**
     * Loads the resource from the plugin with the given name.
     *
     * @param resourceName The name of the resource that should be loaded.
     * @return The URL of the resource.
     * @throws IllegalStateException may be thrown if the jar file has been closed
     * @throws ZipException          if a zip file format error has occurred
     * @throws FileNotFoundException if no files with the given name exists within the jar file
     * @throws IOException           if an I/O error has occurred
     * @throws SecurityException     if any of the jar file entries
     *                               are incorrectly signed.
     */
    public URL getResourceAsURL(String resourceName) throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry(resourceName);
        if (jarEntry == null) {
            throw new FileNotFoundException("No resource found called " + resourceName);
        }
        return new URL("jar:" + file.toURI().toString() + "!/" + resourceName);
    }

    public PluginClassLoader getClassLoader() {
        return classLoader;
    }

    private void checkIfPluginYamlIsValid() throws InvalidPluginException {
        if (!configuration.contains("name")) {
            throw new InvalidPluginException(file.getName() + ": Invalid plugin.yml file, the plugin must have a name value at root!");
        }

        if (!configuration.contains("main")) {
            throw new InvalidPluginException(getName() + ": Invalid plugin.yml file, the plugin must have a main value at root!");
        }

        if (!configuration.contains("version")) {
            throw new InvalidPluginException(getName() + ": Invalid plugin.yml file, the plugin must have a version value at root!");
        }

        if (configuration.contains("requires") && !compareVersion(configuration.getString("requires"))) {
            throw new InvalidPluginException(getName() + ": Invalid plugin.yml file, the plugin requires AvaIre version %s or higher to work correctly!",
                configuration.getString("requires")
            );
        }
    }

    private boolean compareVersion(String version) {
        if (version.equals(AppInfo.getAppInfo().version) || AppInfo.getAppInfo().version.equals("@project.version@")) {
            return true;
        }

        String[] split = version.split("\\.");
        String[] versions = AppInfo.getAppInfo().version.split("\\.");

        for (int i = 0; i < split.length && i < versions.length; i++) {
            if (NumberUtil.parseInt(split[i], 0) < NumberUtil.parseInt(versions[i], 0)) {
                return true;
            }
        }
        return false;
    }
}
