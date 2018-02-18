package com.avairebot.plugin;

import com.avairebot.AvaIre;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.exceptions.InvalidPluginException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.*;
import java.net.URL;
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

    PluginLoader(File file, File dataFolder) throws InvalidPluginException, IOException {
        this.file = file;
        this.dataFolder = dataFolder;

        if (!file.exists()) {
            throw new InvalidPluginException(file.getPath() + " does not exists");
        }

        jarFile = new JarFile(file);

        configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("plugin.yml")));
        checkIfPluginYamlIsValid();

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
     * @param avaire
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
            throw new InvalidPluginException("Invalid plugin.yml file, the plugin must have a name value at root!");
        }

        if (!configuration.contains("main")) {
            throw new InvalidPluginException("Invalid plugin.yml file, the plugin must have a main value at root!");
        }
    }
}
