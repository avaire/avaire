package com.avairebot.orion.plugin;

import com.avairebot.orion.Orion;
import com.avairebot.orion.exceptions.InvalidPluginException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private final File file;
    private final HashMap<String, String> items;
    private final PluginClassLoader classLoader;

    PluginLoader(File file, File dataFolder) throws InvalidPluginException, IOException {
        this.file = file;

        if (!file.exists()) {
            throw new InvalidPluginException(file.getPath() + " does not exists");
        }

        JarFile jarFile = new JarFile(file);

        JarEntry jarEntry = jarFile.getJarEntry("plugin.json");
        if (jarEntry == null) {
            throw new InvalidPluginException(file.getPath() + " does not contain plugin.json", new FileNotFoundException());
        }

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry)));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        items = new Gson().fromJson(sb.toString(),
            new TypeToken<HashMap<String, String>>() {
            }.getType());

        classLoader = new PluginClassLoader(this, Orion.class.getClassLoader(), dataFolder, file);
    }

    public String getName() {
        return items.get("name");
    }

    public String getMain() {
        return items.get("main");
    }

    public void invokePlugin(Orion orion) {
        classLoader.getPlugin().setOrion(orion);
        classLoader.getPlugin().onEnable();
    }
}
