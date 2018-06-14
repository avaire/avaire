package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddExpiresInFieldToBlacklistTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Tue, May 22, 2018 8:56 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.BLACKLIST_TABLE_NAME, "expires_in")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `expires_in` VARCHAR(128) NOT NULL AFTER `reason`;",
                Constants.BLACKLIST_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `expires_in` VARCHAR(128);",
                Constants.BLACKLIST_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "expires_in")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `expires_in`;",
            Constants.BLACKLIST_TABLE_NAME
        ));

        return true;
    }
}
