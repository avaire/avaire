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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashSet;
import java.util.Set;

public class GetGuildCleanup extends SparkRoute {

    private static final Logger log = LoggerFactory.getLogger(GetGuildCleanup.class);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!hasValidAuthorizationHeader(request)) {
            log.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        Set<String> missingGuilds = new HashSet<>();

        for (DataRow row : AvaIre.getInstance().getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME).select("id").get()) {
            if (AvaIre.getInstance().getShardManager().getGuildById(row.getString("id")) == null) {
                missingGuilds.add(row.getString("id"));
            }
        }

        JSONObject root = new JSONObject();
        root.put("ids", missingGuilds);

        return root;
    }
}
