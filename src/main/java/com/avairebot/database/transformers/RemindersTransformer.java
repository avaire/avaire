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
import java.util.Objects;

public class RemindersTransformer extends Transformer
{
    private List<Reminder> reminderList = new ArrayList<Reminder>();


    /**
     * Creates a new transformer instance using
     * the given data row object.
     *
     */
    public RemindersTransformer(Collection collection)
    {
        super(null);

        if (collection != null && !collection.isEmpty()) {
            for (DataRow row : collection) {
                if (row == null) {
                    continue;
                }

                int id = row.getInt("id");
                String message = new String(Base64.getDecoder().decode(row.getString("message")));
                long userId = row.getLong("user_id");
                long guildId = row.getLong("guild_id");
                String channelId = row.getString("channel_id",null);
                Carbon expirationDate = row.getTimestamp("expires_at");

                Reminder reminder = new Reminder(message,id,channelId,userId,guildId,expirationDate);

                reminderList.add(reminder);
            }
        }
    }

    @Nonnull
    public List<Reminder> getReminders()
    {
        return reminderList;
    }



    public class Reminder
    {
        private String message;

        private int id;

        private String channelId;

        private long userId;

        private long guildId;

        private Carbon expirationDate;


        private Reminder(String message, int id, String channelId, long userId, long guildId,Carbon expirationDate)
        {
            this.message = message;
            this.id = id;
            this.channelId = channelId;
            this.userId = userId;
            this.guildId = guildId;
            this.expirationDate = expirationDate;
        }

        public String getMessage()
        {
            return message;
        }

        public int getId()
        {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public long getGuildId() {
            return guildId;
        }

        @CheckForNull
        public String getChannelId()
        {
            return channelId;
        }

        public Carbon getExpirationDate()
        {
            return expirationDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Reminder)) return false;
            Reminder reminder = (Reminder) o;
            return getId() == reminder.getId() &&
                getUserId() == reminder.getUserId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getUserId());
        }
    }
}
