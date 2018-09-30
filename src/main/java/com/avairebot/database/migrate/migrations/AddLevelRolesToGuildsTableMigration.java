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

public class AddLevelRolesToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Tue, Mar 13, 2018 11:55 AM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.GUILD_TABLE_NAME, "level_roles")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `level_roles` TEXT NULL DEFAULT NULL AFTER `level_alerts`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `level_roles` TEXT NULL DEFAULT NULL;",
                Constants.GUILD_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "level_roles")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `level_roles`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
