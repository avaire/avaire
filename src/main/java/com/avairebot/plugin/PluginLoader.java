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

    public String getName() {
        return configuration.getString("name");
    }

    public String getMain() {
        return configuration.getString("main");
    }

    public File getDataFolder() {
        return dataFolder;
    }

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

    public InputStream getResource(String resourceName) throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry(resourceName);
        if (jarEntry == null) {
            throw new FileNotFoundException("No resource found called " + resourceName);
        }
        return jarFile.getInputStream(jarEntry);
    }

    public URL getResourceAsURL(String resourceName) throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry(resourceName);
        if (jarEntry == null) {
            throw new FileNotFoundException("No resource found called " + resourceName);
        }
        return new URL("jar:" + file.toURI().toString() + "!/" + resourceName);
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
