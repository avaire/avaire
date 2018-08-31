package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.commands.system.SetStatusCommand;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

import java.util.Arrays;

public class ChangeGameTask implements Task {

    private int index = 0;

    @Override
    public void handle(AvaIre avaire) {
        if (SetStatusCommand.hasCustomStatus || !avaire.areWeReadyYet()) {
            return;
        }

        if (index >= avaire.getConfig().getStringList("playing").size()) {
            index = 0;
        }

        for (JDA shard : avaire.getShardManager().getShards()) {
            shard.getPresence().setGame(
                getGameFromType(avaire, avaire.getConfig().getStringList("playing").get(index), shard)
            );
        }
        index++;
    }

    private Game getGameFromType(AvaIre avaire, String status, JDA shard) {
        Game game = Game.playing(status);
        if (status.contains(":")) {
            String[] split = status.split(":");
            status = String.join(":", Arrays.copyOfRange(split, 1, split.length));
            if ("listen".equalsIgnoreCase(split[0]) || "listening".equalsIgnoreCase(split[0])) {
                return Game.listening(formatGame(avaire, status, shard));
            } else if ("watch".equalsIgnoreCase(split[0]) || "watching".equalsIgnoreCase(split[0])) {
                return Game.watching(formatGame(avaire, status, shard));
            } else if ("play".equalsIgnoreCase(split[0]) || "playing".equalsIgnoreCase(split[0])) {
                return Game.playing(formatGame(avaire, status, shard));
            } else if ("stream".equalsIgnoreCase(split[0]) || "streaming".equalsIgnoreCase(split[0])) {
                return Game.streaming(formatGame(avaire, status, shard), "https://www.twitch.tv/senither");
            }
        }
        return game;
    }

    private String formatGame(AvaIre avaire, String game, JDA shard) {
        game = game.replaceAll("%users%", NumberUtil.formatNicely(avaire.getShardEntityCounter().getUsers()));
        game = game.replaceAll("%guilds%", NumberUtil.formatNicely(avaire.getShardEntityCounter().getGuilds()));

        game = game.replaceAll("%shard%", shard.getShardInfo().getShardString());
        game = game.replaceAll("%shard-id%", "" + shard.getShardInfo().getShardId());
        game = game.replaceAll("%shard-total%", "" + shard.getShardInfo().getShardTotal());

        return game;
    }
}
