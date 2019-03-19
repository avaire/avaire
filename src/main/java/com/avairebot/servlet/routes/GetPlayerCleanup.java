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

package com.avairebot.servlet.routes;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.database.collection.DataRow;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GetPlayerCleanup extends SparkRoute {

    private static final Logger log = LoggerFactory.getLogger(GetGuildCleanup.class);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!hasValidAuthorizationHeader(request)) {
            log.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        Guild guild = null;
        HashMap<String, Set<String>> missingPlayers = new HashMap<>();
        for (DataRow dataRow : AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
            .select("user_id", "guild_id").orderBy("guild_id").get()) {

            if (!missingPlayers.containsKey(dataRow.getString("guild_id"))) {
                missingPlayers.put(dataRow.getString("guild_id"), new HashSet<>());
            }

            if (guild == null || !guild.getId().equalsIgnoreCase(dataRow.getString("guild_id"))) {
                try {
                    guild = AvaIre.getInstance().getShardManager().getGuildById(dataRow.getString("guild_id"));
                } catch (Exception ignored) {
                    addRowToMissingPlayers(missingPlayers, dataRow);
                }
            }

            if (guild == null) {
                addRowToMissingPlayers(missingPlayers, dataRow);
                continue;
            }

            try {
                if (guild.getMemberById(dataRow.getString("user_id")) == null) {
                    addRowToMissingPlayers(missingPlayers, dataRow);
                }
            } catch (Exception ignored) {
                addRowToMissingPlayers(missingPlayers, dataRow);
            }
        }

        missingPlayers.entrySet().removeIf(next -> next.getValue().isEmpty());

        JSONObject root = new JSONObject();
        root.put("ids", missingPlayers);

        return root;
    }

    private void addRowToMissingPlayers(HashMap<String, Set<String>> missingPlayers, DataRow dataRow) {
        missingPlayers.get(dataRow.getString("guild_id")).add(dataRow.getString("user_id"));
    }
}
