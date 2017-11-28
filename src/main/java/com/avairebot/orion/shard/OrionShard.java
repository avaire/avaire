package com.avairebot.orion.shard;

import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.annotation.Nonnull;

public class OrionShard {

    private final Orion orion;
    private final int shardId;

    @Nonnull
    protected volatile JDA jda;

    public OrionShard(@Nonnull Orion orion, int shardId) {
        this.orion = orion;
        this.shardId = shardId;
        Orion.getLogger().info("Building shard " + shardId);
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
        int total = orion.getConfig().botAuth().getShardsTotal();

        try {
            boolean success = false;
            while (!success) {
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (builder) {
                    builder.useSharding(shardId, total < 1 ? 1 : total);

                    try {
                        orion.getConnectQueue().requestCoin(shardId);
                        
                        newJda = builder.buildAsync();
                        success = true;
                    } catch (RateLimitedException e) {
                        Orion.getLogger().error("Got rate limited while building bot JDA instance! Retrying...", e);
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Orion.getLogger().error("Generic exception when building a JDA instance! Retrying...", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start JDA shard " + shardId, e);
        }

        return newJda;
    }
}
