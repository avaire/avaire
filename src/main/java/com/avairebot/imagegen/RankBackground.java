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
import com.avairebot.contracts.imagegen.BackgroundRankColors;
import com.avairebot.contracts.shop.PurchaseType;
import com.avairebot.imagegen.colors.ranks.*;
import com.avairebot.shared.ExitCodes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RankBackground implements PurchaseType {


    //public static final RankBackground PURPLE
    //    = new RankBackground(2, 10, "Purple", null, new PurpleColors());
    /*
    public static RankBackground DISCORD_DARK
        = new RankBackground(0, 20, "Discord Dark Theme", null, DiscordDarkColors.class);
    public static RankBackground DISCORD_LIGHT =
        new RankBackground(1, 20, "Discord Light Theme", null, DiscordLightColors.class);

    public static RankBackground PIKACHU
        = new RankBackground(10, 50, "Pikachu", "pikachu.jpg", PikachuColors.class);

    public static RankBackground MOUNTAIN_RANGE
        = new RankBackground(11, 50, "Mountain Range", "mountain-range.jpg", MountainRangeColors.class);
    public static RankBackground SAKURA
            = new RankBackground(12, 50, "Sakura", "sakura.jpg", SakuraColors.class);
    public static RankBackground EightBit
        = new RankBackground(13, 50, "8Bit Mountains", "EightBitMountains.png", EightBitMountainColors.class);
    public static RankBackground ABSTRACT
        = new RankBackground(14, 50, "Abstract", "abstract.jpg", AbstractColors.class);
    */



    private final int id;
    private final int cost;
    private final String name;
    private final String file;
    private final BackgroundRankColors instance;


    RankBackground(int id, int cost, String name, String file, BackgroundRankColors instance) {
        this.id = id;
        this.cost = cost;
        this.name = name;
        this.file = file;
        this.instance = instance;
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
     * Gets the name of the rank background.
     *
     * @return The name of the rank background.
     */
    public String getName() {
        return name;
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
      * Gets the background color scheme for the current background image.
      *
      * @return The background color scheme for the current background image.
    */
    public BackgroundRankColors getBackgroundColors() {
        return instance;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Nonnull
    @Override
    public String getPurchaseType() {
        return Constants.RANK_BACKGROUND_PURCHASE_TYPE;
    }
}
