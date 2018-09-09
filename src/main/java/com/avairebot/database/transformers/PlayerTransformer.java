package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;

public class PlayerTransformer extends Transformer {

    private final long userId;
    private final long guildId;

    private String username;
    private String usernameRaw;
    private String discriminator;
    private String avatarId;
    private long experience = 0;

    public PlayerTransformer(long userId, long guildId, DataRow data) {
        super(data);

        this.userId = userId;
        this.guildId = guildId;

        if (hasData()) {
            username = data.getString("username");
            usernameRaw = data.get("username").toString();
            discriminator = data.getString("discriminator");
            avatarId = data.getString("avatar");
            experience = data.getLong("experience", 0);
        }

        reset();
    }

    public long getUserId() {
        return userId;
    }

    public long getGuildId() {
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
