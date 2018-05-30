package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddVotePointsToUsersAndGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Mon, May 07, 2018 10:50 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (!hasColumns(schema, Constants.VOTES_TABLE_NAME, "points", "points_total")) {
            addVoteTableColumns(schema);
        }

        if (!hasColumns(schema, Constants.GUILD_TABLE_NAME, "points")) {
            addGuildsTableColumns(schema);
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return false;
    }

    private void addVoteTableColumns(Schema schema) throws SQLException {
        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `points` INT UNSIGNED NOT NULL DEFAULT '0' AFTER `user_id`, ADD `points_total` INT UNSIGNED NOT NULL DEFAULT '0' AFTER `points`;",
                Constants.VOTES_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `points` INT NOT NULL DEFAULT '0';",
                Constants.VOTES_TABLE_NAME
            ));

            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `points_total` INT NOT NULL DEFAULT '0';",
                Constants.VOTES_TABLE_NAME
            ));
        }
    }

    private void addGuildsTableColumns(Schema schema) throws SQLException {
        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `points` INT UNSIGNED NOT NULL DEFAULT '0' AFTER `icon`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `points` INT NOT NULL DEFAULT '0';",
                Constants.GUILD_TABLE_NAME
            ));
        }
    }

    private boolean hasColumns(Schema schema, String table, String... columns) throws SQLException {
        for (String column : columns) {
            if (!schema.hasColumn(table, column)) {
                return false;
            }
        }
        return true;
    }
}
