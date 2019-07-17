/*
 * Copyright (c) 2019.
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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ResourceLoaderUtil {

    /**
     * Loads the names of all the files and directories under the given resource URL.
     *
     * @param clazz     The clazz instance that should be used to load the resources.
     * @param directory The name of the directory that should have its files listed.
     * @return A list of file and directory names that exists within the given resource URL.
     * @throws IOException If no files where found using the given URL, or the
     *                     given URL is not formatted strictly according to
     *                     to RFC2396 and cannot be converted to a URI.
     */
    @Nonnull
    public static List<String> getFiles(@Nonnull Class<?> clazz, @Nonnull String directory) throws IOException {
        return getFiles(clazz, directory, true);
    }

    /**
     * Loads the names of all the files under the given resource URL, directories can also
     * be included by enabling it using the {@code withSubDirectories} parameter.
     *
     * @param clazz           The clazz instance that should be used to load the resources.
     * @param directory       The name of the directory that should have its files listed.
     * @param withDirectories Determines if folders within the given directory should also be listed.
     * @return A list of file names that exists within the given resource URL.
     * @throws IOException If no files where found using the given URL, or the
     *                     given URL is not formatted strictly according to
     *                     to RFC2396 and cannot be converted to a URI.
     */
    @Nonnull
    public static List<String> getFiles(@Nonnull Class<?> clazz, @Nonnull String directory, boolean withDirectories) throws IOException {
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        URL resourceUrl = clazz.getClassLoader().getResource(directory);

        // If we're loading a resource within a normal file path,
        // we'll just continue to load it normally.
        if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
            return withDirectories
                ? loadResourcesFromFilePath(resourceUrl)
                : loadResourcesFromFilePathWithoutDirectories(resourceUrl);
        }

        if (resourceUrl == null) {
            // Reformats the resource URL to use the class path, and then check if
            // the newly created resource URL actually belongs to a JAR file.
            resourceUrl = formatClassPathToDirectoryStructure(clazz);
            if (resourceUrl == null || !resourceUrl.getProtocol().equals("jar")) {
                throw new IOException("No valid resource URL could be generated from the given class instance");
            }
        }

        // Stripes out the "file:" part of the path, up to the bang separator,
        // which will then be the full path to where the JAR file is located.
        String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));

        // Creates a new JAR file instances using the newly created JAR
        // path, so we can loop through all the JAR file entries.
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));

        Set<String> fileNames = new HashSet<>();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();

            // If the file name doesn't start with out directory, we'll skip it
            // since it's not in the directory we're looking for files in.
            if (!name.startsWith(directory)) {
                continue;
            }

            // Gets the file entry name.
            String entry = name.substring(directory.length());

            // Checks if the file entry is actually a directory.
            int subDirectoryPos = entry.indexOf("/");
            if (subDirectoryPos >= 0) {
                // Skip the file entry if the user specified not to include sub directories,
                // otherwise we'll just only return the directory name.
                if (!withDirectories) {
                    continue;
                }
                entry = entry.substring(0, subDirectoryPos);
            }

            // Skip any entries that are empty.
            if (entry.length() == 0) {
                continue;
            }

            fileNames.add(entry);
        }
        return new ArrayList<>(fileNames);
    }

    /**
     * Loads the name of all the files and directories using the given resource URL.
     *
     * @param resourceUrl The resource URL that should be used to load the files with.
     * @return A list of file names that exists within the given resource URL.
     * @throws IOException If no files where found using the given URL, or the
     *                     given URL is not formatted strictly according to
     *                     to RFC2396 and cannot be converted to a URI.
     */
    private static List<String> loadResourcesFromFilePath(@Nonnull URL resourceUrl) throws IOException {
        String[] files = parseResourceUrlToFile(resourceUrl).list();
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(files);
    }

    /**
     * Loads the name of all the files using the given resource URL, ignoring directories.
     *
     * @param resourceUrl The resource URL that should be used to load the files with.
     * @return A list of file names that exists within the given resource URL.
     * @throws IOException If no files where found using the given URL, or the
     *                     given URL is not formatted strictly according to
     *                     to RFC2396 and cannot be converted to a URI.
     */
    private static List<String> loadResourcesFromFilePathWithoutDirectories(@Nonnull URL resourceUrl) throws IOException {
        File[] files = parseResourceUrlToFile(resourceUrl).listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toList());
    }

    /**
     * Formats the class package path to be a directory path structure instead.
     *
     * @param clazz The class that should be formatted.
     * @return The formatted class package path.
     */
    private static URL formatClassPathToDirectoryStructure(@Nonnull Class<?> clazz) {
        return clazz.getClassLoader().getResource(
            clazz.getName().replace(".", "/") + ".class"
        );
    }

    /**
     * Parses the given resource URL to a file using the resource URI.
     *
     * @param resourceUrl The resource URL that should be parsed to a File instance.
     * @return The file instance that was created from the given resource URL.
     * @throws IOException If the given URL is not formatted strictly according to
     *                     to RFC2396 and cannot be converted to a URI.
     */
    private static File parseResourceUrlToFile(URL resourceUrl) throws IOException {
        try {
            return new File(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid formatted URI used to look for files", e);
        }
    }
}
