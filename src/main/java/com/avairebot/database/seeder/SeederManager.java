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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeederManager {

    private final List<SeederContainer> seeders;

    /**
     * Creates a new seeder manager instance.
     */
    public SeederManager() {
        this.seeders = new ArrayList<>();
    }

    /**
     * Gets all the seeder containers that are currently
     * registered to the seeder manager.
     *
     * @return A list of the registered seeders.
     */
    public List<SeederContainer> getSeeders() {
        return seeders;
    }

    /**
     * Registers the given seeders and stores them as containers in
     * the {@link #seeders seeders} property, they are then used
     * when the {@link #run()} method is called, to seed and
     * populate the database with content.
     *
     * @param seeders The seeder or seeders that should be registered.
     */
    public void register(Seeder... seeders) {
        ENTIRE_LOOP:
        for (Seeder seeder : seeders) {
            for (SeederContainer container : this.seeders) {
                if (container.match(seeder)) {
                    container.setSeeder(seeder);

                    continue ENTIRE_LOOP;
                }
            }

            this.seeders.add(new SeederContainer(seeder));
        }
    }

    /**
     * Runs all the registered seeders, populating the database with data.
     */
    public void run() throws SQLException {
        for (SeederContainer container : seeders) {
            container.getSeeder().run();
        }
    }
}
