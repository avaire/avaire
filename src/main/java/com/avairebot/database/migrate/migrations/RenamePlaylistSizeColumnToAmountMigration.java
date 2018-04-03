package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.SQLite;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class RenamePlaylistSizeColumnToAmountMigration implements Migration {

    @Override
    public String created_at() {
        return "Mon, Feb 12, 2018 2:41 AM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.MUSIC_PLAYLIST_TABLE_NAME, "amount")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof SQLite) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` CHANGE `size` `amount` INT(11) NOT NULL;",
            Constants.MUSIC_PLAYLIST_TABLE_NAME
        ));

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.MUSIC_PLAYLIST_TABLE_NAME, "size")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof SQLite) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` CHANGE `amount` `size` INT(11) NOT NULL;",
            Constants.MUSIC_PLAYLIST_TABLE_NAME
        ));

        return true;
    }
}
