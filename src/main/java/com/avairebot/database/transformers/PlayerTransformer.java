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

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PurchaseController;
import com.avairebot.level.LevelManager;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class PlayerTransformer extends Transformer {

    private final long userId;
    private final long guildId;

    private String username;
    private String usernameRaw;
    private String discriminator;
    private String avatarId;
    private long experience = 0;

    private boolean active = false;

    public PlayerTransformer(long userId, long guildId, DataRow data) {
        super(data);

        this.userId = userId;
        this.guildId = guildId;

        if (hasData()) {
            username = data.getString("username");
            usernameRaw = data.get("username").toString();
            discriminator = data.getString("discriminator");
            avatarId = data.getString("avatar");
            active = data.getBoolean("active", false);

            BigInteger experience = new BigInteger(data.getString("experience", "100"));
            if (experience.compareTo(new BigInteger(String.valueOf(LevelManager.getHardCap()))) >= 0) {
                this.experience = LevelManager.getHardCap();
            } else {
                this.experience = experience.longValue();
            }
        }

        reset();
    }

    public boolean isActive() {
        return active;
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

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public void incrementExperienceBy(long amount) {
        experience = experience + amount;
    }

    public boolean hasPurchases() {
        return getPurchases().hasPurchases();
    }

    @Nonnull
    public PurchasesTransformer getPurchases() {
        return PurchaseController.fetchPurchases(userId);
    }

    @Override
    protected boolean checkIfTransformerHasData() {
        return data != null
            && data.getString("username") != null
            && data.getString("experience") != null
            && data.getString("discriminator") != null;
    }
}
