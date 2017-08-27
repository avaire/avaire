package com.avairebot.orion.commands.system;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class SetStatusCommand extends AbstractCommand {

    public SetStatusCommand(Orion orion) {
        super(orion);
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
    public List<String> getTriggers() {
        return Arrays.asList("setstatus", "status");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            return;
        }

        if (!Game.isValidStreamingUrl(args[0])) {
            event.getJDA().getPresence().setGame(Game.of(String.join(" ", args)));
            return;
        }

        String url = args[0];
        String status = String.join(" ", args).substring(url.length());

        if (args.length == 1) {
            status = "Streaming on Twitch.tv";
        }

        event.getJDA().getPresence().setGame(Game.of(status, url));
    }
}
