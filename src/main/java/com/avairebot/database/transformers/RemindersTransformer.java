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

package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RemindersTransformer extends Transformer {
    private List<Reminder> reminderList = new ArrayList<>();


    /**
     * Creates a new transformer instance using
     * the given data row object.
     */
    public RemindersTransformer(Collection collection) {
        super(null);

        if (collection != null && !collection.isEmpty())
        {
            for (DataRow row : collection)
            {
                if (row == null)
                {
                    continue;
                }

                String id = data.getString("id");
                String message = new String(Base64.getDecoder().decode(data.getString("message")));
                long userId = data.getLong("user_id");
                long guildId = data.getLong("guild_id");
                String channelId = data.getString("channel_id", null);
                Carbon expirationDate = data.getTimestamp("expires_at");

                Reminder reminder = new Reminder(message, id, channelId, userId, guildId, expirationDate);

                reminderList.add(reminder);
            }
        }
    }

    @Nonnull
    public List<Reminder> getReminders() {
        return reminderList;
    }


    private class Reminder {
        private String message;

        private String id;

        private String channelId;

        private long userId;

        private long guildId;

        private Carbon expirationDate;

        private Reminder(String message, String id, String channelId, long userId, long guildId, Carbon expirationDate) {
            this.message = message;
            this.id = id;
            this.channelId = channelId;
            this.userId = userId;
            this.guildId = guildId;
            this.expirationDate = expirationDate;
        }

        public String getMessage() {
            return message;
        }

        public String getId() {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public long getGuildId() {
            return guildId;
        }

        @CheckForNull
        public String getChannelId() {
            return channelId;
        }

        public Carbon getExpirationDate() {
            return expirationDate;
        }

    }
}
