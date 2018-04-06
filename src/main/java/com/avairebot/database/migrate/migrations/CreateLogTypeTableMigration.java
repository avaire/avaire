package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;
import com.avairebot.modules.ModlogModule;

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

        for (ModlogModule.ModlogType type : ModlogModule.ModlogType.values()) {
            createRecord(schema, type);
        }

        return true;
    }

    private void createRecord(Schema schema, ModlogModule.ModlogType type) throws SQLException {
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
