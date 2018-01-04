package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreatePlayerExperiencesTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 9:09 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.PLAYER_EXPERIENCE_TABLE_NAME, table -> {
            table.String("user_id").nullable();
            table.String("guild_id").nullable();
            table.Text("username").nullable();
            table.String("discriminator").nullable();
            table.String("avatar").nullable();
            table.Integer("experience").defaultValue(0);
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.PLAYER_EXPERIENCE_TABLE_NAME);
    }
}
