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
import com.avairebot.database.schema.Schema;
import com.avairebot.modlog.ModlogType;

import java.sql.SQLException;

public class CreateLogTypeTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Fri, Apr 06, 2018 1:03 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (!createTable(schema)) {
            return false;
        }

        for (ModlogType type : ModlogType.values()) {
            createRecord(schema, type);
        }

        return true;
    }

    private void createRecord(Schema schema, ModlogType type) throws SQLException {
        schema.getDbm().newQueryBuilder(Constants.LOG_TYPES_TABLE_NAME)
            .insert(statement -> {
                statement.set("id", type.getId());
                statement.set("name", type.getName());
            });
    }

    private boolean createTable(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.LOG_TYPES_TABLE_NAME, table -> {
            table.Integer("id");
            table.String("name");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.LOG_TYPES_TABLE_NAME);
    }
}
