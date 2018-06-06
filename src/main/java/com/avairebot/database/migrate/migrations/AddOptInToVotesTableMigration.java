package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddOptInToVotesTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Jun 02, 2018 2:48 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.VOTES_TABLE_NAME, "opt_in")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` ADD `opt_in` TINYINT(1) NOT NULL DEFAULT '1';",
            Constants.VOTES_TABLE_NAME
        ));

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.VOTES_TABLE_NAME, "opt_in")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `opt_in`;",
            Constants.VOTES_TABLE_NAME
        ));

        return true;
    }
}
