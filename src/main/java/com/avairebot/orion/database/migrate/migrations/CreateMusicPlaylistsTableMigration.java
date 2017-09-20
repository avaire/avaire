package com.avairebot.orion.database.migrate.migrations;

import com.avairebot.orion.Constants;
import com.avairebot.orion.contracts.database.migrations.Migration;
import com.avairebot.orion.database.schema.Schema;

import java.sql.SQLException;

public class CreateMusicPlaylistsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:17 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.MUSIC_PLAYLIST_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("guild_id");
            table.String("name");
            table.Integer("size");
            table.Text("songs");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.MUSIC_PLAYLIST_TABLE_NAME);
    }
}
