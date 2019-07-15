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

import com.avairebot.contracts.imagegen.BackgroundRankColors;
import com.avairebot.shared.ExitCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankBackgroundHandler
{
    private static final Logger log = LoggerFactory.getLogger(RankBackgroundHandler.class);
    public static final RankBackground DEFAULT_BACKGROUND = new RankBackground(2, 10, "Purple", null, new BackgroundRankColors());
    private static final LinkedHashMap<RankBackground, BackgroundRankColors> backgroundColors = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Integer> namesToCost = new LinkedHashMap<>();
    private static final List<RankBackground> backgrounds = new ArrayList<>();

    private final File backgroundRanksFolder;

    public RankBackgroundHandler() {
        this.backgroundRanksFolder = new File("background_ranks");

        File backgroundsFolder = new File("backgrounds");

        if (!backgroundRanksFolder.exists()) {
            backgroundRanksFolder.mkdirs();
        }

        if(!backgroundsFolder.exists())
        {
            backgroundsFolder.mkdirs();
        }


    }




    public void start()
    {
        Map<String, Integer> unsortedNamesToCost = new HashMap<>();
        backgrounds.add(DEFAULT_BACKGROUND);
        unsortedNamesToCost.put(DEFAULT_BACKGROUND.getName(),DEFAULT_BACKGROUND.getCost());



        try
        {
            for (RankBackground type : getResourceFiles("background_ranks")) {
                    unsortedNamesToCost.put(type.getName(), type.getCost());

                    BackgroundRankColors instance = type.getBackgroundColors();
                    backgroundColors.put(type, instance );
                    backgrounds.add(type);
            }
        }
        catch (IOException e)
        {
            System.out.printf("Invalid cache type given: %s", e.getMessage());
            System.exit(ExitCodes.EXIT_CODE_ERROR);
        }



        unsortedNamesToCost.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> namesToCost.put(entry.getKey(), entry.getValue()));
    }


    private List<RankBackground> getResourceFiles(String folder) throws IOException {
        List<RankBackground> localBackgrounds = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resolverResources = resolver.getResources("classpath:" + folder + "/*.yml");
        for (Resource resource: resolverResources)
        {
            RankBackgroundLoader rank = new RankBackgroundLoader((resource.getFilename()));
            localBackgrounds.add(rank.getRankBackground());
        }

        for (File file : backgroundRanksFolder.listFiles())
        {
            if (file.isDirectory() || file.isHidden()) continue;

            if(file.getName().endsWith(".yml"))
            {
                try
                {
                    log.debug("Attempting to load background: " + file.toString());
                    RankBackgroundLoader pluginLoader = new RankBackgroundLoader(file);

                    localBackgrounds.add(pluginLoader.getRankBackground());
                }
                catch(NullPointerException ex)
                {
                    System.out.println(file.toString());
                    ex.printStackTrace();

                }
            }
        }


        return localBackgrounds;
    }




    public static List<RankBackground> values()
    {
        return backgrounds;
    }

    /**
     * Gets the background color scheme for the current background image.
     *
     * @return The background color scheme for the current background image.
     */
    public static BackgroundRankColors getBackgroundColors(RankBackground rankBackground) {
        return backgroundColors.get(rankBackground);
    }

    /**
     * Gets the default background that should be used for rank commands if none other is set.
     *
     * @return The background color scheme for the current background image.
     */
    public static RankBackground getDefaultBackground() {
        return DEFAULT_BACKGROUND;
    }

    /**
     * Gets the names of all the rank backgrounds as the keys, with
     * the cost of the rank background name as the value.
     *
     * @return A map of all the rank background names, with the cost of the background as the value.
     */
    public static Map<String, Integer> getNameToCost() {
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
    public static RankBackground fromName(@Nonnull String name) {
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
     * @return Possibly-null, the rank background with a matching ID, or {@code NULL}.
     */
    @Nullable
    public static RankBackground fromId(int backgroundId) {
        for (RankBackground background : values()) {
            if (background.getId() == backgroundId) {
                return background;
            }
        }
        return null;
    }

}
