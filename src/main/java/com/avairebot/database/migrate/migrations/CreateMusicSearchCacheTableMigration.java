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

package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.DefaultSQLAction;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateMusicSearchCacheTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sun, Oct 13, 2019 3:45 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        final boolean created = schema.createIfNotExists(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME, table -> {
            table.Integer("provider");
            table.String("query");
            table.LongText("result").nullable();
            table.DateTime("last_lookup_at").defaultValue(new DefaultSQLAction("CURRENT_TIMESTAMP"));
            table.DateTime("created_at").defaultValue(new DefaultSQLAction("CURRENT_TIMESTAMP"));
        });

        if (created && schema.getDbm().getConnection() instanceof MySQL) {
            schema.alterQuery(String.format(
                "ALTER TABLE `%s` ADD PRIMARY KEY(`provider`, `query`);",
                Constants.MUSIC_SEARCH_CACHE_TABLE_NAME
            ));
        }

        return created;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.MUSIC_SEARCH_CACHE_TABLE_NAME);
    }
}
