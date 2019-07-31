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
import com.avairebot.contracts.shop.PurchaseType;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RankBackground implements PurchaseType {

    private final int id;
    private final int cost;
    private final String name;
    private final String backgroundGraphicName;
    private final BackgroundRankColors rankColors;
    private final boolean loadedExternally;

    /**
     * Instantiates a new Rank background that was loaded
     * from inside the AvaIre jar.
     *
     * @param id                    the id of this rank background
     * @param cost                  the cost of the background using vote points
     * @param name                  the name of this background
     * @param backgroundGraphicName the graphic for the background.
     * @param rankColors            the color configuration for this background
     */
    RankBackground(int id, int cost, String name, String backgroundGraphicName, BackgroundRankColors rankColors) {
        this.id = id;
        this.cost = cost;
        this.name = name;
        this.backgroundGraphicName = backgroundGraphicName;
        this.rankColors = rankColors;
        loadedExternally = false;
    }

    /**
     * Instantiates a new Rank background that was loaded
     * from either inside the AvaIre jar or from the external file system
     *
     * @param id                    the id of this rank background
     * @param cost                  the cost of the background using vote points
     * @param name                  the name of this background
     * @param backgroundGraphicName the graphic for the background.
     * @param rankColors            the color configuration for this background
     * @param loadedExternally      whether or not the background was loaded externally
     */
    RankBackground(int id, int cost, String name, String backgroundGraphicName, BackgroundRankColors rankColors, boolean loadedExternally) {
        this.id = id;
        this.cost = cost;
        this.name = name;
        this.backgroundGraphicName = backgroundGraphicName;
        this.rankColors = rankColors;
        this.loadedExternally = loadedExternally;
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
        return backgroundGraphicName;
    }

    /**
     * Gets the background color scheme for the current background image.
     *
     * @return The background color scheme for the current background image.
     */
    public BackgroundRankColors getBackgroundColors() {
        return rankColors;
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

    /**
     * Returns if this background was loaded externally.
     *
     * @return whether or not this file was loaded externally.
     */
    public boolean isLoadedExternally() {
        return loadedExternally;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RankBackground)) {
            return false;
        }

        RankBackground background = (RankBackground) obj;

        return getId() == background.getId()
            || getName().equals(background.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
