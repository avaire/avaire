package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.scheduler.Job;
import com.avairebot.orion.shard.OrionShard;
import net.dv8tion.jda.core.entities.Game;

public class ChangeGameJob extends Job {

    private int index = 0;

    public ChangeGameJob(Orion orion) {
        super(orion, 1);
    }

    @Override
    public void run() {
        if (!orion.areWeReadyYet()) {
            return;
        }

        if (orion.getConfig().getPlaying().size() <= index) {
            index = 0;
        }

        index++;
        for (OrionShard shard : orion.getShards()) {
            shard.getJDA().getPresence().setGame(
                Game.of(formatGame(orion.getConfig().getPlaying().get(index), shard))
            );
        }
    }

    private String formatGame(String game, OrionShard shard) {
        game = game.replaceAll("%users%", "" + orion.getShardEntityCounter().getUsers());
        game = game.replaceAll("%guilds%", "" + orion.getShardEntityCounter().getGuilds());

        game = game.replaceAll("%shard-id%", "" + shard.getShardId());
        game = game.replaceAll("%shard-total%", "" + orion.getConfig().botAuth().getShardsTotal());

        return game;
    }
}
