package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.scheduler.Job;
import net.dv8tion.jda.core.entities.Game;

public class ChangeGameJob extends Job {

    private int index = 0;

    public ChangeGameJob(Orion orion) {
        super(orion, 1);
    }

    @Override
    public void run() {
        if (orion.config.getPlaying().size() <= index) {
            index = 0;
        }

        orion.getJDA().getPresence().setGame(
                Game.of(formatGame(orion.config.getPlaying().get(index++)))
        );
    }

    private String formatGame(String game) {
        game = game.replaceAll("%users%", "" + orion.getJDA().getUsers().size());
        game = game.replaceAll("%guilds%", "" + orion.getJDA().getGuilds().size());

        if (orion.getJDA().getShardInfo() != null) {
            game = game.replaceAll("%shard-id%", "" + orion.getJDA().getShardInfo().getShardId());
            game = game.replaceAll("%shard-total%", "" + orion.getJDA().getShardInfo().getShardTotal());
        }

        return game;
    }
}
