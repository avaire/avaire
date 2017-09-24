package com.avairebot.orion.database.transformers;

import com.avairebot.orion.contracts.database.transformers.Transformer;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.time.Carbon;

public class PlayerTransformer extends Transformer {

    public PlayerTransformer(DataRow data) {
        super(data);
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
        return data.getLong("experience", 0);
    }

    public Carbon getCreatedAt() {
        return data.getTimestamp("created_at");
    }

    public Carbon getUpdatedAt() {
        return data.getTimestamp("updated_at");
    }
}
