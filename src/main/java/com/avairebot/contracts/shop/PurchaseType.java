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

package com.avairebot.contracts.shop;

import com.avairebot.contracts.reflection.Reflectional;

import javax.annotation.Nonnull;

public interface PurchaseType extends Reflectional {

    /**
     * Returns the purchase type, the type is used in the purchases table for
     * tracking what the user bought by type, allowing the user to buy two
     * different things with the same ID, but belonging to different
     * purchases types.
     *
     * @return The purchase type used to represent the object in the database.
     */
    @Nonnull
    String getPurchaseType();

    /**
     * Returns the cost of the item, the cost will be used
     * to determine if the user can buy the item.
     *
     * @return The cost of the item.
     */
    int getCost();
}
