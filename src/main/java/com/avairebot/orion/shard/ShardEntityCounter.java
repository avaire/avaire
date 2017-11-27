package com.avairebot.orion.shard;

import com.avairebot.orion.Orion;

public class ShardEntityCounter {

    private final Orion orion;

    private final ShardEntity guilds = new ShardEntity(shard -> shard.getJDA().getGuilds().size());
    private final ShardEntity textChannels = new ShardEntity(shard -> shard.getJDA().getTextChannels().size());
    private final ShardEntity voiceChannels = new ShardEntity(shard -> shard.getJDA().getVoiceChannels().size());
    private final ShardEntity users = new ShardEntity(shard -> shard.getJDA().getUsers().size());

    public ShardEntityCounter(Orion orion) {
        this.orion = orion;
    }

    /**
     * Gets the total amount of guilds shared between all the shards of the bot.
     *
     * @return The total amount of guilds for the bot.
     */
    public long getGuilds() {
        return guilds.getValue(orion);
    }

    /**
     * Gets the total amount of text channels shared between all the shards of the bot.
     *
     * @return The total amount of text channels for the bot.
     */
    public long getTextChannels() {
        return textChannels.getValue(orion);
    }

    /**
     * Gets the total amount of voice channels shared between all shards of the bot.
     *
     * @return The total amount of voice channels for the bot.
     */
    public long getVoiceChannels() {
        return voiceChannels.getValue(orion);
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
        return users.getValue(orion);
    }
}
