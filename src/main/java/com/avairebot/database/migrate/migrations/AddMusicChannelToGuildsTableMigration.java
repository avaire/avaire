package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.connections.MySQL;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class AddMusicChannelToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Thu, May 17, 2018 1:27 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (hasColumns(schema)) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `music_channel_text` VARCHAR(64) NULL DEFAULT NULL AFTER `level_channel`, "
                    + "ADD `music_channel_voice` VARCHAR(64) NULL DEFAULT NULL AFTER `music_channel_text`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `music_channel_text` VARCHAR(64) NULL DEFAULT NULL;",
                Constants.GUILD_TABLE_NAME
            ));

            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `music_channel_voice` VARCHAR(64) NULL DEFAULT NULL;",
                Constants.GUILD_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!hasColumns(schema)) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `music_channel_text`, DROP `music_channel_voice`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }

    private boolean hasColumns(Schema schema) throws SQLException {
        return schema.hasColumn(Constants.GUILD_TABLE_NAME, "music_channel_text")
            && schema.hasColumn(Constants.GUILD_TABLE_NAME, "music_channel_voice");
    }
}
