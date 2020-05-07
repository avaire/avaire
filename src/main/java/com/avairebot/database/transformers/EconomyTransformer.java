/*
 * Copyright (c) 2020.
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
import com.avairebot.database.controllers.EconomyController;
import com.avairebot.level.LevelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class EconomyTransformer extends Transformer
{
    private static final Logger log = LoggerFactory.getLogger(EconomyTransformer.class);


    private long balance = 0;

    private boolean active = false;

    private int dailyClaims = 0;

    private String username;
    private String usernameRaw;
    private String discriminator;
    private String avatarId;

    private long userId;
    private long guildId;

    /**
     * Creates a new transformer instance using
     * the given data row object.
     *
     * @param data The data row object that should be used
     *             for creating the transformer instance.
     */
    public EconomyTransformer(DataRow data)
    {
        super(data);

        if (hasData())
        {
            BigInteger balanceAmount = new BigInteger(data.getString("balance", "100"));
            active = data.getBoolean("active", true);
            dailyClaims = data.getInt("dailyClaims");
            username = data.getString("username");
            usernameRaw = data.get("username").toString();
            discriminator = data.getString("discriminator");
            avatarId = data.getString("avatar");
            userId = data.getLong("user_id");
            guildId = data.getLong("guild_id");

            if (balanceAmount.compareTo(new BigInteger(String.valueOf(LevelManager.getHardCap()))) >= 0)
            {
                this.balance = LevelManager.getHardCap();
            }
            else
            {
                this.balance = balanceAmount.longValue();
            }
        }
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance)
    {
        this.balance = balance;
    }

    public void incrementBalanceBy(long amount)
    {
        this.balance += amount;
        EconomyController.updateBalance(AvaIre.getInstance(),guildId,userId,balance);
    }

    public void decrementBalanceBy(long amount)
    {
        this.balance -= amount;
        EconomyController.updateBalance(AvaIre.getInstance(),guildId,userId,balance);
    }

    public boolean isActive() {
        return active;
    }

    public int getDailyClaims()
    {
        return dailyClaims;
    }

    public String getAvatar() {
        return avatarId;
    }



    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getUsernameRaw() {
        return usernameRaw;
    }

    public long getUserId() {
        return userId;
    }

    public long getGuildId() {
        return guildId;
    }

    @Override
    protected boolean checkIfTransformerHasData() {
        return data != null
            && data.getString("balance") != null;
    }

    public void setUsername(String name)
    {
        username = name;
    }

    public void setDiscriminator(String discriminator)
    {
        this.discriminator = discriminator;
    }


    public void setAvatar(String avatarId)
    {
        this.avatarId = avatarId;
    }
}
