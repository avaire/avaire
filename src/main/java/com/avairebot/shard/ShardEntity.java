package com.avairebot.shard;

import com.avairebot.AvaIre;
import com.avairebot.contracts.shard.EntityGenerator;

public class ShardEntity {

    private final EntityGenerator generator;

    private long value = 0;
    private long lastUpdate = -1;

    ShardEntity(EntityGenerator generator) {
        this.generator = generator;
    }

    public long getValue(AvaIre avaire) {
        if (isOutdated()) {
            long count = 0;
            for (AvaireShard shard : avaire.getShards()) {
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
