package com.avairebot.orion.contracts.shard;

import com.avairebot.orion.shard.OrionShard;

public interface EntityGenerator {

    long generateEntity(OrionShard shard);
}
