package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateShardsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:25 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.SHARDS_TABLE_NAME, table -> {
            table.Integer("id");
            table.Integer("count");
            table.String("address").nullable();
            table.String("version").nullable();
            table.Integer("users").defaultValue(0);
            table.Integer("guilds").defaultValue(0);
            table.Integer("channels").defaultValue(0);
            table.Integer("voices").defaultValue(0);
            table.Integer("songs").defaultValue(0);
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.SHARDS_TABLE_NAME);
    }
}
