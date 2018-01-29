package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;

public class PlayerTransformer extends Transformer {

    private String username;
    private String discriminator;
    private String avatarId;
    private long experience = 0;

    public PlayerTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            username = data.getString("username");
            discriminator = data.getString("discriminator");
            avatarId = data.getString("avatar");

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
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getAvatar() {
        return avatarId;
    }

    public void setAvatar(String avatarId) {
        this.avatarId = avatarId;
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
