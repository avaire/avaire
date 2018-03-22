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
