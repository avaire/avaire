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

public class GetGuilds extends SparkRoute {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String[] ids = request.params("ids").split(",");

        JSONObject root = new JSONObject();
        for (String id : ids) {
            try {
                Guild guildById = AvaIre.getInstance().getShardManager().getGuildById(Long.parseLong(id));
                if (guildById == null) {
                    root.put(id, JSONObject.NULL);
                    continue;
                }

                JSONObject guild = new JSONObject();

                guild.put("id", guildById.getId());
                guild.put("name", guildById.getName());
                guild.put("region", guildById.getRegion().getName());
                guild.put("icon", guildById.getIconUrl());

                JSONObject owner = new JSONObject();
                owner.put("id", guildById.getOwner().getUser().getId());
                owner.put("username", guildById.getOwner().getUser().getName());
                owner.put("discriminator", guildById.getOwner().getUser().getDiscriminator());
                owner.put("avatar", guildById.getOwner().getUser().getEffectiveAvatarUrl());
                guild.put("owner", owner);

                JSONObject counter = new JSONObject();

                long userCount = guildById.getMembers()
                    .stream().filter(member -> member.getUser().isBot()).count();

                counter.put("textChannels", guildById.getTextChannels().size());
                counter.put("voiceChannels", guildById.getVoiceChannels().size());
                counter.put("members", guildById.getMembers().size());
                counter.put("users", guildById.getMembers().size() - userCount);
                counter.put("bots", userCount);
                guild.put("counter", counter);

                root.put(id, guild);
            } catch (NumberFormatException e) {
                root.put(id, JSONObject.NULL);
            }
        }

        return root;
    }
}
