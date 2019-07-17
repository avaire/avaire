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

import com.avairebot.config.YamlConfiguration;
import com.avairebot.contracts.imagegen.BackgroundRankColors;
import com.avairebot.utilities.ColorUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * The type Rank background loader.
 */
public class RankBackgroundLoader
{
    private final YamlConfiguration config;
    private RankBackground background;

    /**
     * Instantiates a new Rank background loader
     * that uses the Jar filesystem.
     *
     * @param backgroundResource the background resource
     */
    public RankBackgroundLoader(@Nonnull String backgroundResource) {
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream("backgrounds/" + backgroundResource)));




        this.background = new RankBackground(config.getInt("id"),config.getInt("cost"),config.getString("name"),
                                                config.getString("backgroundImage"), getBackgroundColors());
    }

    /**
     * Instantiates a new Rank background loader
     * using the external file system.
     *
     * @param backgroundResource the background resource
     * @throws FileNotFoundException if the file cannot be found
     */
    public RankBackgroundLoader(@Nonnull File backgroundResource) throws FileNotFoundException {
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(backgroundResource)));

        this.background = new RankBackground(config.getInt("id"),config.getInt("cost"),config.getString("name"),
            config.getString("backgroundImage"), getBackgroundColors(), true);
    }

    private BackgroundRankColors getBackgroundColors()
    {
        BackgroundRankColors colors = new BackgroundRankColors();
        Color backgroundColor = loadColorFromYaml("colors.backgroundColor");
        colors.setBackgroundColor(backgroundColor);
        Color mainTextColor = loadColorFromYaml("colors.mainTextColor");
        colors.setMainTextColor(mainTextColor);
        Color secondaryTextColor = loadColorFromYaml("colors.secondaryTextColor");
        colors.setSecondaryTextColor(secondaryTextColor);
        Color experienceBackgroundColor = loadColorFromYaml("colors.experienceBackgroundColor");
        colors.setExperienceBackgroundColor(experienceBackgroundColor);
        Color experienceForegroundColor = loadColorFromYaml("colors.experienceForegroundColor");
        colors.setExperienceForegroundColor(experienceForegroundColor);
        Color experienceSeparatorColor = loadColorFromYaml("colors.experienceSeparatorColor");
        colors.setExperienceSeparatorColor(experienceSeparatorColor);

        if(config.contains("colors.backgroundCoverColor"))
        {
            Color backgroundCoverColor = loadColorFromYaml("colors.backgroundCoverColor");
            colors.setBackgroundCoverColor(backgroundCoverColor);
        }
        if(config.contains("colors.experienceTextColor"))
        {
            Color experienceTextColor = loadColorFromYaml("colors.experienceTextColor");
            colors.setExperienceTextColor(experienceTextColor);
        }

        return colors;
    }

    private Color loadColorFromYaml(String heading)
    {
        int red = config.getInt(heading + ".red");
        int green = config.getInt(heading + ".green");
        int blue = config.getInt(heading + ".blue");
        if(config.contains((heading + ".alpha")))
        {
            int alpha = config.getInt(heading + ".alpha");
            return ColorUtil.makeColor(red,green,blue,alpha);
        }
        else
        {
            return ColorUtil.makeColor(red,green,blue);
        }
    }

    /**
     * Gets the rank background.
     *
     * @return The background rank for this container
     */
    public RankBackground getRankBackground() {
        return background;
    }


    /**
     * Gets the language configuration, the config can be used to
     * load strings, lists, and values directly off the language.
     *
     * @return The language configuration.
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return background.getName();
    }
}
