package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.shard.AvaireShard;
import net.dv8tion.jda.core.entities.Game;

import java.util.Arrays;

public class ChangeGameJob extends Job {

    private int index = 0;

    public ChangeGameJob(AvaIre avaire) {
        super(avaire, 1);
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        if (index >= avaire.getConfig().getPlaying().size()) {
            index = 0;
        }

        for (AvaireShard shard : avaire.getShards()) {
            shard.getJDA().getPresence().setGame(
                getGameFromType(avaire.getConfig().getPlaying().get(index), shard)
            );
        }
        index++;
    }

    private Game getGameFromType(String status, AvaireShard shard) {
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

    private String formatGame(String game, AvaireShard shard) {
        game = game.replaceAll("%users%", "" + avaire.getShardEntityCounter().getUsers());
        game = game.replaceAll("%guilds%", "" + avaire.getShardEntityCounter().getGuilds());

        game = game.replaceAll("%shard-id%", "" + shard.getShardId());
        game = game.replaceAll("%shard-total%", "" + avaire.getSettings().getShardCount());

        return game;
    }
}
