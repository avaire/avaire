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

package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ReactionTransformer extends Transformer {

    private final Map<Long, Long> roles = new HashMap<>();

    private long guildId = -1;
    private long channelId = -1;
    private long messageId = -1;

    public ReactionTransformer(DataRow row) {
        super(row);

        if (row != null) {
            guildId = row.getLong("guild_id");
            channelId = row.getLong("channel_id");
            messageId = row.getLong("message_id");

            if (data.getString("roles", null) != null) {
                HashMap<Long, Long> dbRoles = AvaIre.gson.fromJson(
                    data.getString("roles"),
                    new TypeToken<HashMap<Long, Long>>() {
                    }.getType());

                for (Map.Entry<Long, Long> item : dbRoles.entrySet()) {
                    roles.put(item.getKey(), item.getValue());
                }
            }
        }
    }

    @Nullable
    public Long getRoleIdFromEmote(@Nonnull Emote emote) {
        if (!roles.containsKey(emote.getIdLong())) {
            return null;
        }
        return roles.get(emote.getIdLong());
    }

    public void addReaction(@Nonnull Emote emote, @Nonnull Role role) {
        roles.put(emote.getIdLong(), role.getIdLong());
    }

    public boolean removeReaction(@Nonnull Emote emote) {
        if (roles.containsKey(emote.getIdLong())) {
            roles.remove(emote.getIdLong());
            return true;
        }
        return false;
    }

    public Map<Long, Long> getRoles() {
        return roles;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getMessageId() {
        return messageId;
    }
}
