package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.scheduler.Job;
import com.avairebot.orion.shard.OrionShard;
import net.dv8tion.jda.core.entities.Game;

import java.util.Arrays;

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

        if (index >= orion.getConfig().getPlaying().size()) {
            index = 0;
        }

        for (OrionShard shard : orion.getShards()) {
            shard.getJDA().getPresence().setGame(
                getGameFromType(orion.getConfig().getPlaying().get(index), shard)
            );
        }
        index++;
    }

    private Game getGameFromType(String status, OrionShard shard) {
        Game game = Game.playing(status);
        if (status.contains(":")) {
            String[] split = status.split(":");
            status = String.join(":", Arrays.copyOfRange(split, 1, split.length));
            switch (split[0].toLowerCase()) {
                case "listen":
                case "listening":
                    return Game.listening(formatGame(status, shard));

                case "watch":
                case "watching":
                    return Game.watching(formatGame(status, shard));

                case "play":
                case "playing":
                    return Game.playing(formatGame(status, shard));

                case "stream":
                case "streaming":
                    return Game.streaming(formatGame(status, shard), "https://www.twitch.tv/senither");
            }
        }
        return game;
    }

    private String formatGame(String game, OrionShard shard) {
        game = game.replaceAll("%users%", "" + orion.getShardEntityCounter().getUsers());
        game = game.replaceAll("%guilds%", "" + orion.getShardEntityCounter().getGuilds());

        game = game.replaceAll("%shard-id%", "" + shard.getShardId());
        game = game.replaceAll("%shard-total%", "" + orion.getConfig().botAuth().getShardsTotal());

        return game;
    }
}
