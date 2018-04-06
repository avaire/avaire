package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateLogTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Fri, Apr 06, 2018 1:04 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.LOG_TABLE_NAME, table -> {
            table.Integer("type", 2);
            table.Integer("modlogCase");
            table.String("guild_id");
            table.String("user_id");
            table.String("target_id").nullable();
            table.String("message_id").nullable();
            table.String("reason").nullable();
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.LOG_TABLE_NAME);
    }
}
