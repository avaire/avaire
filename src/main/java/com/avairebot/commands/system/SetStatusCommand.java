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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.scheduler.tasks.ChangeGameTask;
import com.avairebot.utilities.ComparatorUtil;
import net.dv8tion.jda.core.entities.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetStatusCommand extends SystemCommand {

    public SetStatusCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Set Status Command";
    }

    @Override
    public String getDescription() {
        return "Sets the status of the bot instance for all servers the bot is on, if no status is set the bot status will go back to cycling status from the config.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <game>` - Sets the bots playing status to the given game.",
            "`:command <twitch url>` - The URL that the bot should be broadcasting."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command with some stuff`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ResetStatusTypesCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("setstatus", "status");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM_ROLE;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            context.makeInfo(
                "The bot status cycle has been re-enabled, the change game job can now change the bot status again."
            ).queue(newMessage -> ChangeGameTask.hasCustomStatus = false);

            return true;
        }

        if (ComparatorUtil.isFuzzyFalse(String.join(" ", args))) {
            ChangeGameTask.hasCustomStatus = true;
            avaire.getShardManager().setGame(null);

            context.makeSuccess("The status message has been **disabled**")
                .queue();

            return true;
        }

        Game game = parseGame(args);
        avaire.getShardManager().setGame(game);

        context.makeSuccess("Changed status to **:type :status**")
            .set("type", getTypeAsString(game.getType()))
            .set("status", game.getName())
            .queue(newMessage -> ChangeGameTask.hasCustomStatus = true);

        return true;
    }

    private String getTypeAsString(Game.GameType type) {
        switch (type) {
            case STREAMING:
                return "Streaming";

            case WATCHING:
                return "Watching";

            case LISTENING:
                return "Listening to";

            default:
                return "Playing";
        }
    }

    private Game parseGame(String[] args) {
        if (Game.isValidStreamingUrl(args[0])) {
            String url = args[0];
            String streamStatus = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                : "on Twitch.tv!";

            return Game.streaming(streamStatus, url);
        }

        String status = String.join(" ", args);

        if (status.contains(":")) {
            String[] split = status.split(":");
            status = String.join(":", Arrays.copyOfRange(split, 1, split.length));
            switch (split[0].toLowerCase()) {
                case "listen":
                case "listening":
                    return Game.listening(status);

                case "watch":
                case "watching":
                    return Game.watching(status);

                case "play":
                case "playing":
                    return Game.playing(status);

                case "stream":
                case "streaming":
                    String streamUrl = "https://www.twitch.tv/senither";
                    if (split.length > 1) {
                        String[] parts = split[1].split(" ");
                        streamUrl = "https://www.twitch.tv/" + parts[0];
                        status = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    }
                    return Game.streaming(status, streamUrl);
            }
        }

        return Game.playing(status);
    }
}
