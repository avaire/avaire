package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddModlogToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Mon, Feb 12, 2018 3:11 AM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.GUILD_TABLE_NAME, "modlog") && schema.hasColumn(Constants.GUILD_TABLE_NAME, "modlog_case")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `modlog` VARCHAR(32) NULL DEFAULT NULL AFTER `autorole`, ADD `modlog_case` INT NOT NULL DEFAULT '0' AFTER `modlog`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `modlog` VARCHAR(32) NULL DEFAULT NULL;",
                Constants.GUILD_TABLE_NAME
            ));

            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `modlog_case` INT NOT NULL DEFAULT '0';",
                Constants.GUILD_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "modlog") && !schema.hasColumn(Constants.GUILD_TABLE_NAME, "modlog_case")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `modlog`, DROP `modlog_case`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
