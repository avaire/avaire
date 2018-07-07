package com.avairebot.utilities;

import com.avairebot.Settings;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JarUtil {

    @Nullable
    public static ProcessBuilder rebuildJarExecution(Settings settings) throws URISyntaxException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File("AvaIre.jar");
        if (!currentJar.exists() || !currentJar.isFile()) {
            return null;
        }

        final List<String> command = new ArrayList<>();
        command.add(javaBin);

        command.addAll(settings.getRuntimeArgs());
        command.add("-jar");
        command.add(currentJar.getPath());
        command.addAll(settings.getJarArgs());

        return new ProcessBuilder(command);
    }
}
