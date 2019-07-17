/*
 * Copyright (c) 2019.
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

package com.avairebot.database.seeder.seeders;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.database.seeder.Seeder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GuildTypeTableSeeder extends Seeder {

    public GuildTypeTableSeeder(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String table() {
        return Constants.GUILD_TYPES_TABLE_NAME;
    }

    @Override
    public void run() throws SQLException {
        if (!tableHasValue("name", "VIP")) {
            createRecord("VIP", 10, 50, 50, 30, 30, 8, 8);
        }

        if (!tableHasValue("name", "VIP+")) {
            createRecord("VIP+", 25, 100, 150, 60, 50, 15, 15);
        }
    }

    private void createRecord(
        String name,
        int playlistLists,
        int playlistSongs,
        int aliases,
        int selfAssignableRoles,
        int levelRoles,
        int reactionRoleMessages,
        int reactionRoleReactions
    ) throws SQLException {
        Map<String, Integer> playlist = new HashMap<>();
        playlist.put("lists", playlistLists);
        playlist.put("songs", playlistSongs);

        Map<String, Integer> reactionRoles = new HashMap<>();
        reactionRoles.put("messages", reactionRoleMessages);
        reactionRoles.put("rolesPerMessage", reactionRoleReactions);

        Map<String, Object> limits = new HashMap<>();

        limits.put("playlist", playlist);
        limits.put("aliases", aliases);
        limits.put("selfAssignableRoles", selfAssignableRoles);
        limits.put("reactionRoles", reactionRoles);
        limits.put("levelRoles", levelRoles);

        createQuery().insert(statement -> {
            statement.set("name", name);
            statement.set("limits", AvaIre.gson.toJson(limits));
        });
    }
}
