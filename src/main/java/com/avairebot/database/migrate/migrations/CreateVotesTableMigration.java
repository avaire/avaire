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

package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.DatabaseEngine;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateVotesTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Thu, Mar 22, 2018 2:08 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.VOTES_TABLE_NAME, table -> {
            table.String("user_id", 32);
            table.String("expires_in", 128);

            table.setEngine(DatabaseEngine.InnoDB);
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.VOTES_TABLE_NAME);
    }
}
