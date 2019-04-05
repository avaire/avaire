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
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashSet;
import java.util.Set;

public class PostGuildCleanup extends SparkRoute {

    private static final Logger log = LoggerFactory.getLogger(PostGuildCleanup.class);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!hasValidAuthorizationHeader(request)) {
            log.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        Set<Long> idsToDelete = new HashSet<>();
        JSONObject obj = new JSONObject(request.body());

        for (Object id : obj.getJSONArray("ids").toList()) {
            try {
                long idLong = Long.parseLong(id.toString());

                Guild guild = AvaIre.getInstance().getShardManager().getGuildById(idLong);
                if (guild != null) {
                    continue;
                }

                idsToDelete.add(idLong);
            } catch (NumberFormatException ignored) {

            }
        }

        String query = String.format("DELETE FROM `%s` WHERE `id` = ?",
            Constants.GUILD_TABLE_NAME
        );

        log.debug("Starting \"Guild Cleanup\" route task with query: " + query);

        AvaIre.getInstance().getDatabase().queryBatch(query, statement -> {
            for (Long id : idsToDelete) {
                statement.setLong(1, id);
                statement.addBatch();
            }
        });

        log.debug("Finished \"Guild Cleanup\" route task, deleted {} records in the process", idsToDelete.size());

        return buildResponse(response, 200, String.format("Done, successfully deleted %s records", idsToDelete.size()));
    }
}
