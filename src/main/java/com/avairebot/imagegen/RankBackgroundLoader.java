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
package com.avairebot.imagegen;

import com.avairebot.config.YamlConfiguration;
import com.avairebot.utilities.ColorUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class RankBackgroundLoader {

    private final YamlConfiguration config;
    private RankBackground background;

    /**
     * Instantiates a new Rank background loader that loads a resource
     * from anywhere in the filesystem outside the jar,
     * given the full path.
     *
     * @param backgroundResource A path to the background resource in the filesystem.
     * @throws FileNotFoundException If the file cannot be found.
     */
    RankBackgroundLoader(@Nonnull File backgroundResource) throws FileNotFoundException {
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(backgroundResource)));

        int id = config.getInt("id");
        int cost = config.getInt("cost");

        String name = config.getString("name");
        String backgroundImage = config.getString("backgroundImage");

        background = new RankBackground(id, cost, name, backgroundImage, getColors());
    }

    /**
     * Looks for and reads the following headings in the colors heading
     * into a {@link com.avairebot.imagegen.BackgroundRankColors} instance.
     * <p>
     * <ul>
     * <li>backgroundColor</li>
     * <li>secondaryTextColor</li>
     * <li>experienceBackgroundColor</li>
     * <li>experienceForegroundColor</li>
     * <li>experienceSeparatorColor</li>
     * </ul>
     * <p>
     * If the optional backgroundCoverColor and experienceTextColor headings exist,
     * then those values are read as well.
     */
    private BackgroundRankColors getColors() {
        BackgroundRankColors colors = new BackgroundRankColors();
        colors.setBackgroundColor(loadColorFromYaml("colors.backgroundColor"));
        colors.setMainTextColor(loadColorFromYaml("colors.mainTextColor"));
        colors.setSecondaryTextColor(loadColorFromYaml("colors.secondaryTextColor"));
        colors.setExperienceBackgroundColor(loadColorFromYaml("colors.experienceBackgroundColor"));
        colors.setExperienceForegroundColor(loadColorFromYaml("colors.experienceForegroundColor"));
        colors.setExperienceSeparatorColor(loadColorFromYaml("colors.experienceSeparatorColor"));

        if (config.contains("colors.backgroundCoverColor")) {
            colors.setBackgroundCoverColor(loadColorFromYaml("colors.backgroundCoverColor"));
        }

        if (config.contains("colors.experienceTextColor")) {
            colors.setExperienceTextColor(loadColorFromYaml("colors.experienceTextColor"));
        }

        return colors;
    }

    /*
     * Scans the provided color heading and looks for
     * red, green, and blue sub heading values with in the range (0 - 255).
     * If the alpha heading exists, the alpha color is loaded and must be in the range of (0-100).
     * Returns an sRGB color representing the color heading.
     */
    private Color loadColorFromYaml(String heading) {
        int red = config.getInt(heading + config.options().pathSeparator() + "red");
        int green = config.getInt(heading + config.options().pathSeparator() + "green");
        int blue = config.getInt(heading + config.options().pathSeparator() + "blue");

        if (!config.contains((heading + config.options().pathSeparator() + "alpha"))) {
            return ColorUtil.makeColor(red, green, blue);
        }

        return ColorUtil.makeColor(red, green, blue,
            config.getInt(heading + ".alpha")
        );
    }

    /**
     * Returns the rank background information loaded by this container
     * including the id, cost, name, background graphics, color scheme,
     * and where this configuration was loaded from.
     *
     * @return The rank background information
     */
    public RankBackground getRankBackground() {
        return background;
    }

    /**
     * Gets the Rank Background configuration, the config
     * can be used to directly query the id, cost, name, background graphics, color scheme,
     * and where this configuration was loaded from.
     *
     * @return The rank background configuration.
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return background.getName();
    }
}
