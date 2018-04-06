package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddDJLevelToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Mon, Jan 15, 2018 11:28 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.GUILD_TABLE_NAME, "dj_level")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `dj_level` INT NOT NULL DEFAULT '0' AFTER `modules`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `dj_level` INT NOT NULL DEFAULT '0';",
                Constants.GUILD_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "dj_level")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `dj_level`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
