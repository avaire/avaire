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
import com.avairebot.contracts.metrics.SparkRoute;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class GetGuildsExists extends SparkRoute {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String[] ids = request.params("ids").split(",");

        JSONObject root = new JSONObject();
        for (String id : ids) {
            try {
                Guild guildById = AvaIre.getInstance().getShardManager().getGuildById(Long.parseLong(id));
                if (guildById == null) {
                    root.put(id, false);
                    continue;
                }
                root.put(id, true);
            } catch (NumberFormatException e) {
                root.put(id, false);
            }
        }

        return root;
    }
}
