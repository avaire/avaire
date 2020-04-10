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
import com.avairebot.database.migrate.Migrations;
import com.avairebot.database.schema.Schema;
import com.avairebot.time.Carbon;

import java.sql.SQLException;

public class CreateRemindersTableMigration implements Migration
{


    /**
     * Gets the time the migration was created at, this is used to order
     * migrations, making sure migrations are rolled out to the
     * database and back in the right order.
     * <p>
     * The time format can be any of the supported carbon time formats.
     *
     * @return the carbon time string
     * @see Carbon
     */
    @Override
    public String created_at() {
        return "Tue, July 16, 2019 12:38 PM";
    }

    /**
     * Attempts to migrate the database, this is automatically executed from the
     * {@link Migrations#up() migrate up} method.
     *
     * @param schema the database schematic instance
     * @return the result of the schematic instance call
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.REMINDERS_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("user_id");
            table.String("channel_id").nullable();
            table.String("message");
            table.Boolean("sent").defaultValue(false);
            table.DateTime("expires_at");
            table.DateTime("stored_at");
            table.Timestamps();
        });
    }

    /**
     * Attempts to rollback the migrations from the database, this is automatically executed from the
     * {@link Migrations#down() down()} and
     * {@link Migrations#rollback(int) rollback(int)} method.
     *
     * @param schema the database schematic instance
     * @return the result of the schematic instance call
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.REMINDERS_TABLE_NAME);
    }
}
