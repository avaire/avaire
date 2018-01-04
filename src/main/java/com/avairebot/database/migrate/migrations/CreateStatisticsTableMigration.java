package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateStatisticsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:19 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (!createTable(schema)) {
            return false;
        }

        return !schema.getDbm().newQueryBuilder(Constants.STATISTICS_TABLE_NAME)
            .insert(statement -> statement.set("respects", 0))
            .isEmpty();
    }

    private boolean createTable(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.STATISTICS_TABLE_NAME, table -> {
            table.Integer("respects").defaultValue(0);
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.STATISTICS_TABLE_NAME);
    }
}
