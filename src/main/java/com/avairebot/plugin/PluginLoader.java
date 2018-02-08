package com.avairebot.plugin;

import com.avairebot.AvaIre;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.exceptions.InvalidPluginException;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private final File file;
    private final PluginClassLoader classLoader;
    private final YamlConfiguration configuration;

    PluginLoader(File file, File dataFolder) throws InvalidPluginException, IOException {
        this.file = file;

        if (!file.exists()) {
            throw new InvalidPluginException(file.getPath() + " does not exists");
        }

        JarFile jarFile = new JarFile(file);

        JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
        if (jarEntry == null) {
            throw new InvalidPluginException(file.getPath() + " does not contain plugin.yml", new FileNotFoundException());
        }

        configuration = YamlConfiguration.loadConfiguration(
            new InputStreamReader(jarFile.getInputStream(jarEntry))
        );
        checkIfPluginYamlIsValid();

        classLoader = new PluginClassLoader(this, AvaIre.class.getClassLoader(), dataFolder, file);
    }

    public String getName() {
        return configuration.getString("name");
    }

    public String getMain() {
        return configuration.getString("main");
    }

    public void invokePlugin(AvaIre avaire) {
        classLoader.getPlugin().init(avaire);
        classLoader.getPlugin().onEnable();
    }

    public void registerEventListeners(JDABuilder jda) {
        if (!classLoader.getPlugin().getEventListeners().isEmpty()) {
            for (ListenerAdapter adapter : classLoader.getPlugin().getEventListeners()) {
                jda.addEventListener(adapter);
            }
        }
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
