package com.avairebot.orion;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class OrionShard {

    private static final Logger log = LoggerFactory.getLogger(OrionShard.class);

    private final Orion orion;
    private final int shardId;

    @Nonnull
    protected volatile JDA jda;

    public OrionShard(@Nonnull Orion orion, int shardId) {
        this.orion = orion;
        this.shardId = shardId;
        log.info("Building shard " + shardId);
        jda = buildJDA(ShardBuilder.getDefaultShardBuilder(orion));
    }

    @Nonnull
    public JDA getJDA() {
        return jda;
    }

    public int getShardId() {
        return shardId;
    }

    private JDA buildJDA(final JDABuilder builder) {
        JDA newJda = null;

        try {
            boolean success = false;
            while (!success) {
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (builder) {
                    builder.useSharding(shardId, 2); // Make the shard total a config option

                    try {
                        newJda = builder.buildAsync();
                        success = true;
                    } catch (RateLimitedException e) {
                        log.error("Got rate limited while building bot JDA instance! Retrying...", e);
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("Generic exception when building a JDA instance! Retrying...", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start JDA shard " + shardId, e);
        }

        return newJda;
    }
}
