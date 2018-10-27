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
import com.avairebot.imagegen.colors.ranks.MountainRangeColors;
import com.avairebot.imagegen.colors.ranks.PikachuColors;
import com.avairebot.imagegen.colors.ranks.PurpleColors;
import com.avairebot.shared.ExitCodes;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

public enum RankBackgrounds {

    PURPLE(0, null, PurpleColors.class),
    PIKACHU(1, "pikachu.jpg", PikachuColors.class),
    MAUNTAIN_RANGE(2, "mountain-range.jpg", MountainRangeColors.class);

    private static final RankBackgrounds DEFAULT_BACKGROUND = RankBackgrounds.PURPLE;
    private static final EnumMap<RankBackgrounds, BackgroundRankColors> backgroundColors = new EnumMap<>(RankBackgrounds.class);

    static {
        for (RankBackgrounds type : values()) {
            try {
                backgroundColors.put(type, type.getClassInstance().getDeclaredConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.printf("Invalid cache type given: %s", e.getMessage());
                System.exit(ExitCodes.EXIT_CODE_ERROR);
            }
        }
    }

    private final int id;
    private final String file;
    private final Class<? extends BackgroundRankColors> instance;

    RankBackgrounds(int id, String file, Class<? extends BackgroundRankColors> instance) {
        this.id = id;
        this.file = file;
        this.instance = instance;
    }

    /**
     * Gets the default background that should be used for rank commands if none other is set.
     *
     * @return The background color scheme for the current background image.
     */
    public static RankBackgrounds getDefaultBackground() {
        return DEFAULT_BACKGROUND;
    }

    /**
     * Gets the ID for the image, can be used as a reference in the database.
     *
     * @return The image ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the background image file name.
     *
     * @return The background image file name.
     */
    public String getBackgroundFile() {
        return file;
    }

    /**
     * Gets the background image color scheme instance.
     *
     * @return The background image color scheme instance.
     */
    public Class<? extends BackgroundRankColors> getClassInstance() {
        return instance;
    }

    /**
     * Gets the background color scheme for the current background image.
     *
     * @return The background color scheme for the current background image.
     */
    public BackgroundRankColors getBackgroundColors() {
        return backgroundColors.get(this);
    }
}
