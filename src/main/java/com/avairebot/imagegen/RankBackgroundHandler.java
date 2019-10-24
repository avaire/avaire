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
import com.avairebot.config.YamlConfiguration;
import com.avairebot.shared.ExitCodes;
import com.avairebot.utilities.ResourceLoaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class RankBackgroundHandler {

    private static RankBackgroundHandler instance;
    private final Logger log = LoggerFactory.getLogger(RankBackgroundHandler.class);

    private final LinkedHashMap<String, Integer> namesToCost = new LinkedHashMap<>();
    private final List<RankBackground> backgrounds = new ArrayList<>();
    private final List<Integer> usedIds = new ArrayList<>();
    private final List<String> usedNames = new ArrayList<>();
    private final File backgroundsDirectory;

    private RankBackgroundHandler() {
        backgroundsDirectory = new File("backgrounds");

        if (!backgroundsDirectory.exists()) {
            backgroundsDirectory.mkdirs();
        }

        copyBackgrounds();
    }

    /**
     * Returns an instance of the rank background handler,
     * responsible for initializing and loading all the rank
     * backgrounds.
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
     * Returns the purchase type for rank backgrounds, the type is used in the
     * purchases table for tracking what the user bought by type, allowing
     * the user to buy two different things with the same ID, but
     * belonging to different purchases types.
     *
     * @return The purchase type used to represent rank backgrounds in the database.
     */
    public static String getPurchaseType() {
        return Constants.RANK_BACKGROUND_PURCHASE_TYPE;
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
        for (RankBackground background : backgrounds) {
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
        for (RankBackground background : backgrounds) {
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
            log.error("Invalid cache type given: {}", e.getMessage(), e);
            System.exit(ExitCodes.EXIT_CODE_ERROR);
        }

        unsortedNamesToCost.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> namesToCost.put(entry.getKey(), entry.getValue()));
    }

    /**
     * Compares the contents of the resources folder with the listed files
     * in the background-index.yaml and if new backgrounds are found they
     * get copied over and appended onto the background-index.yaml file.
     * <p>
     * If for some reason the background-index does not exist, every file
     * in the resources folder gets replaced and written onto
     * the background-index.yaml file.
     */
    private void copyBackgrounds() {
        try {
            File indexFile = new File(backgroundsDirectory, "background-index.yml");
            if (!indexFile.exists()) {
                indexFile.createNewFile();
            }

            YamlConfiguration index = YamlConfiguration.loadConfiguration(indexFile);

            List<String> resourceFiles = ResourceLoaderUtil.getFiles(RankBackgroundHandler.class, "backgrounds");
            for (String file : resourceFiles) {
                if (!index.getList("index", Collections.EMPTY_LIST).contains(file)) {
                    InputStream inputStream = RankBackgroundHandler.class.getClassLoader().getResourceAsStream("backgrounds/" + file);
                    Files.copy(inputStream, Paths.get("backgrounds/" + file), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            index.set("index", resourceFiles);

            try {
                index.save(indexFile);
            } catch (IOException e) {
                log.error("Failed to write resource files to background index: {}", e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error("Failed to copy over rank backgrounds files: {}", e.getMessage(), e);
        }
    }

    /**
     * Scans the contents of the backgrounds folder in the current
     * running directory and attempts to load each single one.
     * <p>
     * Afterwards if the backgrounds folder in the current working directory
     * did not already exist when the program began executing, it will load
     * all the backgrounds prepackaged in the backgrounds/resource folder
     * of the currently running jar.
     * <p>
     * It then returns a list of every single background found and loaded.
     */
    @SuppressWarnings("ConstantConditions")
    private List<RankBackground> getResourceFiles() throws IOException {
        List<RankBackground> localBackgrounds = new ArrayList<>();

        for (File file : backgroundsDirectory.listFiles()) {
            if (!isValidBackgroundsFile(file)) {
                continue;
            }

            log.debug("Attempting to load background from file system: {}", file.toString());

            RankBackgroundLoader rankBackgroundLoader = new RankBackgroundLoader(file);
            RankBackground background = rankBackgroundLoader.getRankBackground();

            BackgroundValidation validation = validateRankBackgroundRank(background);
            if (!validation.passed) {
                log.warn("The \"{}\" rank background failed the {} validation check, refusing to load", file.getName(), validation.name());

                continue;
            }

            usedIds.add(background.getId());
            usedNames.add(background.getName());
            localBackgrounds.add(background);
            log.debug("Loaded background from file system: " + file.toString());
        }

        return localBackgrounds;
    }

    private boolean isValidBackgroundsFile(File file) {
        return !file.isDirectory()
            && !file.isHidden()
            && file.getName().endsWith(".yml")
            && !file.getName().equals("background-index.yml");
    }

    private BackgroundValidation validateRankBackgroundRank(RankBackground background) {
        if (background.getCost() <= 0) {
            return BackgroundValidation.INVALID_COST;
        }

        if (background.getName() == null || background.getName().isEmpty()) {
            return BackgroundValidation.INVALID_NAME;
        }

        if (usedNames.contains(background.getName())) {
            return BackgroundValidation.NAME_ALREADY_USED;
        }

        if (usedIds.contains(background.getId())) {
            return BackgroundValidation.ID_ALREADY_USED;
        }

        return BackgroundValidation.PASSED;
    }

    enum BackgroundValidation {

        INVALID_COST,
        INVALID_NAME,
        NAME_ALREADY_USED,
        ID_ALREADY_USED,
        PASSED(true);

        private final boolean passed;

        BackgroundValidation() {
            this(false);
        }

        BackgroundValidation(boolean passed) {
            this.passed = passed;
        }
    }
}
