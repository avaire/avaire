/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.database.migrate.migrations;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CreateGuildTypeTableMigration implements Migration {

    private int id = 1;

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 5:23 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (!createTable(schema)) {
            return false;
        }

        return schema.getDbm().newQueryBuilder(Constants.GUILD_TYPES_TABLE_NAME).insert(
            createRecord("VIP", 10, 50, 50, 30, 30, 8, 8),
            createRecord("VIP+", 25, 100, 150, 60, 50, 15, 15)
        ).size() == 2;
    }

    private Map<String, Object> createRecord(
        String name,
        int playlistLists,
        int playlistSongs,
        int aliases,
        int selfAssignableRoles,
        int levelRoles,
        int reactionRoleMessages,
        int reactionRoleReactions
    ) {
        Map<String, Integer> playlist = new HashMap<>();
        playlist.put("lists", playlistLists);
        playlist.put("songs", playlistSongs);

        Map<String, Integer> reactionRoles = new HashMap<>();
        reactionRoles.put("messages", reactionRoleMessages);
        reactionRoles.put("rolesPerMessage", reactionRoleReactions);

        Map<String, Object> limits = new HashMap<>();
        Map<String, Object> items = new HashMap<>();

        limits.put("playlist", playlist);
        limits.put("aliases", aliases);
        limits.put("selfAssignableRoles", selfAssignableRoles);
        limits.put("reactionRoles", reactionRoles);
        limits.put("levelRoles", levelRoles);

        items.put("id", id++);
        items.put("name", name);
        items.put("limits", AvaIre.gson.toJson(limits));

        return items;
    }

    private boolean createTable(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.GUILD_TYPES_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("name");
            table.Text("limits");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.GUILD_TYPES_TABLE_NAME);
    }
}
