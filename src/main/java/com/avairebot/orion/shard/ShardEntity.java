package com.avairebot.orion.shard;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.shard.EntityGenerator;

public class ShardEntity {

    private final EntityGenerator generator;

    private long value = 0;
    private long lastUpdate = -1;

    ShardEntity(EntityGenerator generator) {
        this.generator = generator;
    }

    public long getValue(Orion orion) {
        if (isOutdated()) {
            long count = 0;
            for (OrionShard shard : orion.getShards()) {
                count += generator.generateEntity(shard);
            }
            value = count;

            lastUpdate = System.currentTimeMillis() + 15000;
        }

        return value;
    }

    private boolean isOutdated() {
        return lastUpdate < System.currentTimeMillis();
    }
}
