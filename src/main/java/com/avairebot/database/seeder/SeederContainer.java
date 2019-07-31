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

package com.avairebot.database.seeder;

import com.avairebot.contracts.database.seeder.Seeder;

public class SeederContainer {

    private String name;
    private Seeder seeder;

    public SeederContainer(Seeder seeder) {
        this.seeder = seeder;

        name = seeder.getClass().getSimpleName();
    }

    /**
     * Gets the seeder name.
     *
     * @return the name of the seeder
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the plugin the seeder is registered to.
     *
     * @param name the name of the plugin the seeder is registered to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the seeder instance linked to the container.
     *
     * @return the seeder instance linked to the container
     */
    public Seeder getSeeder() {
        return seeder;
    }

    /**
     * Sets the seeder instance for the container.
     *
     * @param seeder the seeder the container is linked to
     */
    public void setSeeder(Seeder seeder) {
        this.seeder = seeder;
    }

    /**
     * Checks to see if the provided seeder matches the container.
     *
     * @param seeder the seeder to compare with
     * @return either (1) true if the seeder matches
     *         or (2) false if it doesn't match.
     */
    public boolean match(Seeder seeder) {
        return getName() != null && seeder.getClass().getName().equals(getName());
    }
}
