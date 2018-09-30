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
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateMusicPlaylistsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:17 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        boolean isMySQL = schema.getDbm().getConnection().getConnection() instanceof MySQL;

        return schema.createIfNotExists(Constants.MUSIC_PLAYLIST_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("guild_id");
            table.String("name");

            if (isMySQL) {
                table.Integer("size");
            } else {
                table.Integer("amount");
            }

            table.Text("songs");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.MUSIC_PLAYLIST_TABLE_NAME);
    }
}
