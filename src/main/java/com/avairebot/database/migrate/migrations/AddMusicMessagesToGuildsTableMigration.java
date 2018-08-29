package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddMusicMessagesToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "2018-08-28 22:45:05";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `music_messages` TINYINT(1) NOT NULL DEFAULT '1' AFTER `music_channel_voice`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `music_messages` TINYINT(1) NOT NULL DEFAULT '1';",
                Constants.GUILD_TABLE_NAME
            ));
        }
        return false;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "music_messages")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `music_messages`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
