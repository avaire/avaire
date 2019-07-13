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
import java.io.InputStreamReader;

public class RankBackgroundLoader
{
    private final YamlConfiguration config;
    private RankBackground background;

    public RankBackgroundLoader(@Nonnull String backgroundResource) {
        //this.background = background;

        config = YamlConfiguration.loadConfiguration(new InputStreamReader(
            //getClass().getClassLoader().getResourceAsStream(("background_ranks/" + background.getName() + ".yml"))
            getClass().getClassLoader().getResourceAsStream("background_ranks/" + backgroundResource)));




        this.background = new RankBackground(config.getInt("id"),config.getInt("cost"),config.getString("name"),
                                                config.getString("backgroundImage"), getBackgroundColors());


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
        colors.setExperienceForegroundColor(experienceSeparatorColor);

        if(config.contains("colors.backgroundCoverColor"))
        {
            Color backgroundCoverColor = loadColorFromYaml("colors.backgroundCoverColor");
            colors.setExperienceForegroundColor(backgroundCoverColor);
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
