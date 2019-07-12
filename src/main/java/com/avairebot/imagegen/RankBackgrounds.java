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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum RankBackgrounds implements PurchaseType {

    DISCORD_DARK(0, 20, "Discord Dark Theme", null, DiscordDarkColors.class),
    DISCORD_LIGHT(1, 20, "Discord Light Theme", null, DiscordLightColors.class),
    PURPLE(2, 10, "Purple", null, PurpleColors.class),
    PIKACHU(10, 50, "Pikachu", "pikachu.jpg", PikachuColors.class),
    MOUNTAIN_RANGE(11, 50, "Mountain Range", "mountain-range.jpg", MountainRangeColors.class),
    SAKURA(12, 50, "Sakura", "sakura.jpg", SakuraColors.class),
    EightBit(13, 50, "8Bit Mountains", "EightBitMountains.png", EightBitMountainColors.class),
    ABSTRACT(14, 50, "Abstract", "abstract.jpg", AbstractColors.class);


    private static final RankBackgrounds DEFAULT_BACKGROUND = RankBackgrounds.PURPLE;
    private static final EnumMap<RankBackgrounds, BackgroundRankColors> backgroundColors = new EnumMap<>(RankBackgrounds.class);
    private static final LinkedHashMap<String, Integer> namesToCost = new LinkedHashMap<>();

    static {
        Map<String, Integer> unsortedNamesToCost = new HashMap<>();

        for (RankBackgrounds type : values()) {
            try {
                unsortedNamesToCost.put(type.getName(), type.getCost());
                backgroundColors.put(type, type.getClassInstance().getDeclaredConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.printf("Invalid cache type given: %s", e.getMessage());
                System.exit(ExitCodes.EXIT_CODE_ERROR);
            }
        }

        unsortedNamesToCost.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> namesToCost.put(entry.getKey(), entry.getValue()));
    }

    private final int id;
    private final int cost;
    private final String name;
    private final String file;
    private final Class<? extends BackgroundRankColors> instance;

    RankBackgrounds(int id, int cost, String name, String file, Class<? extends BackgroundRankColors> instance) {
        this.id = id;
        this.cost = cost;
        this.name = name;
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
     * Gets the names of all the rank backgrounds as the keys, with
     * the cost of the rank background name as the value.
     *
     * @return A map of all the rank background names, with the cost of the background as the value.
     */
    public static LinkedHashMap<String, Integer> getNameToCost() {
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
    public static RankBackgrounds fromName(@Nonnull String name) {
        for (RankBackgrounds background : values()) {
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
    public static RankBackgrounds fromId(int backgroundId) {
        for (RankBackgrounds background : values()) {
            if (background.getId() == backgroundId) {
                return background;
            }
        }
        return null;
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
