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

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The type Rank background.
 */
public class RankBackground implements PurchaseType {

    private final int id;
    private final int cost;
    private final String name;
    private final String file;
    private final BackgroundRankColors instance;
    private final boolean loadedExternally;


    /**
     * Instantiates a new Rank background.
     *
     * @param id       the id
     * @param cost     the cost
     * @param name     the name
     * @param file     the file
     * @param instance the instance
     */
    RankBackground(int id, int cost, String name, String file, BackgroundRankColors instance) {
        this.id = id;
        this.cost = cost;
        this.name = name;
        this.file = file;
        this.instance = instance;
        loadedExternally = false;
    }

    /**
     * Instantiates a new Rank background.
     *
     * @param id               the id
     * @param cost             the cost
     * @param name             the name
     * @param file             the file
     * @param instance         the instance
     * @param loadedExternally whether or not it was loaded externally
     */
    RankBackground(int id, int cost, String name, String file, BackgroundRankColors instance, boolean loadedExternally) {
        this.id = id;
        this.cost = cost;
        this.name = name;
        this.file = file;
        this.instance = instance;
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


    /**
     * Returns if this background was loaded externally.
     *
     * @return whether or not this file way loaded externally.
     */
    public boolean isLoadedExternally() {
        return loadedExternally;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RankBackground)) return false;
        RankBackground that = (RankBackground) o;
        return getId() == that.getId() ||
            getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId(), getName());
    }
}
