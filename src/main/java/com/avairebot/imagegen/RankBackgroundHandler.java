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

package com.avairebot.imagegen;

import com.avairebot.Constants;
import com.avairebot.shared.ExitCodes;
import com.avairebot.utilities.ResourceLoaderUtil;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class RankBackgroundHandler {

    private static RankBackgroundHandler instance;
    private final Logger log = LoggerFactory.getLogger(RankBackgroundHandler.class);
    private final LinkedHashMap<String, Integer> namesToCost = new LinkedHashMap<>();
    private final List<RankBackground> backgrounds = new ArrayList<>();
    private final List<Integer> usedIds = new ArrayList<>();
    private final List<String> usedNames = new ArrayList<>();
    private final boolean backgroundsFolderAlreadyExists;
    private File backgroundsFolder;

    private RankBackgroundHandler() {
        backgroundsFolder = new File("backgrounds");

        backgroundsFolderAlreadyExists = backgroundsFolder.exists();
        if (!backgroundsFolderAlreadyExists) {
            backgroundsFolder.mkdirs();
            copyBackgroundsFromJarToFolder();
        }
        copyNewBackgroundsOver();
    }

    /**
     * Returns an instance of the rank background handler
     * class.
     *
     * @return An instance of the handler
     */
    public static RankBackgroundHandler getInstance() {
        if (instance == null) {
            instance = new RankBackgroundHandler();
        }
        return instance;
    }

    /**
     * Returns the purchase type of rank background.
     *
     * @return The rank purchase type
     */
    public static String getRankPurchaseType() {
        return Constants.RANK_BACKGROUND_PURCHASE_TYPE;
    }

    /**
     * Returns a list of all the
     * rank backgrounds loaded by AvaIre.
     *
     * @return The list of all rank backgrounds.
     */
    public List<RankBackground> values() {
        return backgrounds;
    }

    /**
     * Gets the names of all the rank backgrounds as the keys, with
     * the cost of the rank background name as the value.
     *
     * @return A map of all the rank background names, with the cost of the background as the value.
     */
    public Map<String, Integer> getNameToCost() {
        return namesToCost;
    }

    /**
     * Gets the rank background with the given name, the check uses a loss check
     * by ignoring letter casing and just checking if the letters match.
     *
     * @param name The name of the background that should be returned.
     * @return Possibly {@code NULL}, or the background with a matching name.
     */
    @Nullable
    public RankBackground fromName(@Nonnull String name) {
        for (RankBackground background : values()) {
            if (background.getName().equalsIgnoreCase(name)) {
                return background;
            }
        }
        return null;
    }

    /**
     * Gets the rank background with the given ID, if no rank backgrounds were
     * found with the given ID, {@code NULL} will be returned instead.
     *
     * @param backgroundId The ID of the rank background that should be returned.
     * @return Possibly -null, the rank background with a matching ID, or {@code NULL}.
     */
    @Nullable
    public RankBackground fromId(int backgroundId) {
        for (RankBackground background : values()) {
            if (background.getId() == backgroundId) {
                return background;
            }
        }
        return null;
    }

    /**
     * Initializes the rank background containers, loading all the rank
     * background information into memory, this method should only
     * be called once during the startup of the bot.
     */
    public void start() {
        Map<String, Integer> unsortedNamesToCost = new HashMap<>();

        try {
            for (RankBackground type : getResourceFiles()) {
                unsortedNamesToCost.put(type.getName(), type.getCost());
                backgrounds.add(type);
            }
        } catch (IOException e) {
            System.out.printf("Invalid cache type given: %s", e.getMessage());
            System.exit(ExitCodes.EXIT_CODE_ERROR);
        }

        unsortedNamesToCost.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> namesToCost.put(entry.getKey(), entry.getValue()));
    }

    private void copyBackgroundsFromJarToFolder() {
        try {
            List<String> files = ResourceLoaderUtil.getFiles(RankBackgroundHandler.class, "backgrounds");
            for (String file : files) {
                File actualFile = new File("backgrounds/" + file);
                InputStream inputStream = RankBackgroundHandler.class.getClassLoader().getResourceAsStream("backgrounds/" + file);
                if (!actualFile.exists()) {
                    Files.copy(inputStream, Paths.get("backgrounds/" + file), StandardCopyOption.REPLACE_EXISTING);
                    writeToIndex(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ExitCodes.EXIT_CODE_ERROR);
        }
    }

    private void writeToIndex(String newFile)
    {
        Path filePath = Paths.get("backgrounds/image-index");


        try
        {
            if(!filePath.toFile().exists())
            {
                Files.write(filePath,(newFile + System.lineSeparator()).getBytes(Charsets.UTF_8)
                            ,StandardOpenOption.CREATE);
            }
            else
            {
                Files.write(filePath,(System.lineSeparator() + newFile + System.lineSeparator()).getBytes(Charsets.UTF_8)
                    ,StandardOpenOption.APPEND);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void copyNewBackgroundsOver()
    {
        try
        {
            List<String> resourceFiles = ResourceLoaderUtil.getFiles(RankBackgroundHandler.class, "backgrounds");
            Path filePath = Paths.get("backgrounds/image-index");
            List<String> oldFiles = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String file: resourceFiles)
            {
                if(!oldFiles.contains(file))
                {
                    InputStream inputStream = RankBackgroundHandler.class.getClassLoader().getResourceAsStream("backgrounds/" + file);
                    Files.copy(inputStream, Paths.get("backgrounds/" + file), StandardCopyOption.REPLACE_EXISTING);
                    writeToIndex(file);
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private List<RankBackground> getResourceFiles() throws IOException {
        List<RankBackground> localBackgrounds = new ArrayList<>();

        for (File file : backgroundsFolder.listFiles()) {
            if (file.isDirectory() || file.isHidden()) continue;

            if (file.getName().endsWith(".yml")) {
                try {
                    log.debug("Attempting to load background from file system: " + file.toString());
                    RankBackgroundLoader rankBackgroundLoader = new RankBackgroundLoader(file);
                    RankBackground background = rankBackgroundLoader.getRankBackground();
                    if (isBackgroundRankValid(background)) {
                        usedIds.add(background.getId());
                        usedNames.add(background.getName());
                        localBackgrounds.add(background);
                        log.debug("Loaded background from file system: " + file.toString());
                    } else {
                        log.debug("Background invalid from file system; refusing to load : " + file.toString());
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!backgroundsFolderAlreadyExists) {
            List<String> files = ResourceLoaderUtil.getFiles(RankBackgroundHandler.class, "backgrounds");

            for (String file : files) {
                if (file.endsWith(".yml")) {
                    RankBackgroundLoader rank = new RankBackgroundLoader(file);
                    RankBackground rankBackground = rank.getRankBackground();
                    log.debug("Attempting to load background from resource folder: " + file);
                    if (isBackgroundRankValid(rankBackground)) {
                        usedIds.add(rankBackground.getId());
                        usedNames.add(rankBackground.getName());
                        localBackgrounds.add(rankBackground);
                        log.debug("Loaded background from resource folder: " + file);
                    } else {
                        log.debug("Background from resource folder invalid; refusing to load : " + file);
                    }
                }
            }
        }

        return localBackgrounds;
    }

    private boolean isBackgroundRankValid(RankBackground background) {
        return background.getCost() > 0
            && background.getName() != null
            && !background.getName().isEmpty()
            && !usedNames.contains(background.getName())
            && !usedIds.contains(background.getId());
    }
}
