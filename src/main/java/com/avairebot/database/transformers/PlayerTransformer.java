package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;

public class PlayerTransformer extends Transformer {

    private long experience = 0;

    public PlayerTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            experience = data.getLong("experience", 0);
        }
    }

    public String getUserId() {
        return data.getString("user_id");
    }

    public String getGuildId() {
        return data.getString("guild_id");
    }

    public String getUsername() {
        return data.getString("username");
    }

    public String getDiscriminator() {
        return data.getString("discriminator");
    }

    public String getAvatar() {
        return data.getString("avatar");
    }

    public long getExperience() {
        return experience;
    }

    public Carbon getCreatedAt() {
        return data.getTimestamp("created_at");
    }

    public Carbon getUpdatedAt() {
        return data.getTimestamp("updated_at");
    }

    public void incrementExperienceBy(int amount) {
        experience = experience + amount;
    }
}
