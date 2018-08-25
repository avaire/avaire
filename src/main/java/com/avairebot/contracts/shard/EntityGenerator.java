package com.avairebot.contracts.shard;

import net.dv8tion.jda.core.JDA;

public interface EntityGenerator {

    /**
     * Generates the shard entity value for the given shard, this is used
     * for quickly calculating the total value between shards for common
     * things by the {@link com.avairebot.shard.ShardEntityCounter ShardEntityCounter}.
     *
     * @param shard The shard that should be used to calculate the shard entity.
     * @return The entity value for the given shard.
     */
    long generateEntity(JDA shard);
}
