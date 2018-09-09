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

package com.avairebot.shard;

import com.avairebot.AvaIre;

public class ShardEntityCounter {

    private final AvaIre avaire;

    private final ShardEntity guilds = new ShardEntity(shard -> shard.getGuilds().size());
    private final ShardEntity textChannels = new ShardEntity(shard -> shard.getTextChannels().size());
    private final ShardEntity voiceChannels = new ShardEntity(shard -> shard.getVoiceChannels().size());
    private final ShardEntity users = new ShardEntity(shard -> shard.getUsers().size());

    public ShardEntityCounter(AvaIre avaire) {
        this.avaire = avaire;
    }

    /**
     * Gets the total amount of guilds shared between all the shards of the bot.
     *
     * @return The total amount of guilds for the bot.
     */
    public long getGuilds() {
        return guilds.getValue(avaire);
    }

    /**
     * Gets the total amount of text channels shared between all the shards of the bot.
     *
     * @return The total amount of text channels for the bot.
     */
    public long getTextChannels() {
        return textChannels.getValue(avaire);
    }

    /**
     * Gets the total amount of voice channels shared between all shards of the bot.
     *
     * @return The total amount of voice channels for the bot.
     */
    public long getVoiceChannels() {
        return voiceChannels.getValue(avaire);
    }

    /**
     * Gets the total amount of text and voice channels shared between all shards of the bot.
     *
     * @return The total amount of text and voice channels for the bot.
     */
    public long getChannels() {
        return getTextChannels() + getVoiceChannels();
    }

    /**
     * Gets the total amount of users shared between all shards of the bot.
     *
     * @return The total amount of users for the bot.
     */
    public long getUsers() {
        return users.getValue(avaire);
    }
}
