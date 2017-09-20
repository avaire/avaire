package com.avairebot.orion.database.migrate.migrations;

import com.avairebot.orion.Constants;
import com.avairebot.orion.contracts.database.migrations.Migration;
import com.avairebot.orion.database.schema.Schema;

import java.sql.SQLException;

public class CreateBlacklistTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:06 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.BLACKLIST_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("user_id").nullable();
            table.String("reason").nullable();
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.BLACKLIST_TABLE_NAME);
    }
}
