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
