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
import com.avairebot.database.connections.SQLite;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class RenamePlaylistSizeColumnToAmountMigration implements Migration {

    @Override
    public String created_at() {
        return "Mon, Feb 12, 2018 2:41 AM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.MUSIC_PLAYLIST_TABLE_NAME, "amount")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof SQLite) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` CHANGE `size` `amount` INT(11) NOT NULL;",
            Constants.MUSIC_PLAYLIST_TABLE_NAME
        ));

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.MUSIC_PLAYLIST_TABLE_NAME, "size")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof SQLite) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` CHANGE `amount` `size` INT(11) NOT NULL;",
            Constants.MUSIC_PLAYLIST_TABLE_NAME
        ));

        return true;
    }
}
