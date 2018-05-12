package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;

public class PlayerTransformer extends Transformer {

    private String userId;
    private String guildId;

    private String username;
    private String usernameRaw;
    private String discriminator;
    private String avatarId;
    private long experience = 0;

    public PlayerTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            userId = data.getString("user_id");
            guildId = data.getString("guild_id");

            username = data.getString("username");
            usernameRaw = data.get("username").toString();
            discriminator = data.getString("discriminator");
            avatarId = data.getString("avatar");

            experience = data.getLong("experience", 0);
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameRaw() {
        return usernameRaw;
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

    public void incrementExperienceBy(int amount) {
        experience = experience + amount;
    }
}
