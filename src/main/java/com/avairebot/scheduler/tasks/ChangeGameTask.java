/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

import java.util.Arrays;

public class ChangeGameTask implements Task {

    public static boolean hasCustomStatus = false;

    private int index = 0;

    @Override
    public void handle(AvaIre avaire) {
        if (hasCustomStatus || !avaire.areWeReadyYet()) {
            return;
        }

        if (index >= avaire.getConfig().getStringList("playing").size()) {
            index = 0;
        }

        String playing = avaire.getConfig().getStringList("playing").get(index);

        if (playing.trim().length() != 0) {
            for (JDA shard : avaire.getShardManager().getShards()) {
                shard.getPresence().setGame(getGameFromType(avaire, playing, shard));
            }
        } else {
            avaire.getShardManager().setGame(null);
        }

        index++;
    }

    private Game getGameFromType(AvaIre avaire, String status, JDA shard) {
        if (!status.contains(":")) {
            return Game.playing(formatGame(avaire, status, shard));
        }

        String[] split = status.split(":");
        status = String.join(":", Arrays.copyOfRange(split, 1, split.length));
        switch (split[0].toLowerCase()) {
            case "listen":
            case "listening":
                return Game.listening(formatGame(avaire, status, shard));

            case "watch":
            case "watching":
                return Game.watching(formatGame(avaire, status, shard));

            case "stream":
            case "streaming":
                return Game.streaming(formatGame(avaire, status, shard), "https://www.twitch.tv/senither");

            default:
                return Game.playing(formatGame(avaire, status, shard));
        }
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
