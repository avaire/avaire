package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;

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
    public List<String> getTriggers() {
        return Arrays.asList("setstatus", "status");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return false;
        }

        Game game = parseGame(args);
        message.getJDA().getPresence().setGame(game);

        MessageFactory.makeSuccess(message, "Changed status to **:type :status**")
            .set("type", getTypeAsString(game.getType()))
            .set("status", game.getName())
            .queue();

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
            String streamStatus = String.join(" ", args).substring(url.length());

            if (args.length == 1) {
                streamStatus = "Streaming on Twitch.tv";
            }

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
                    return Game.streaming(status, "https://www.twitch.tv/senither");
            }
        }

        return Game.playing(status);
    }
}
