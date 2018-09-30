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

import java.sql.SQLException;

public class RecreateFeedbackTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Jun 23, 2018 4:29 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        schema.dropIfExists(Constants.FEEDBACK_TABLE_NAME);

        return schema.create(Constants.FEEDBACK_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("user_id", 32);
            table.String("channel_id", 32).nullable();
            table.Text("message");
            table.Text("response").nullable();
            table.String("response_id", 32).nullable();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        // We don't really care to create the feedback table again
        // at this point, since it wasn't used anyway.
        return schema.dropIfExists(Constants.FEEDBACK_TABLE_NAME);
    }
}
