package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class PingCommand extends AbstractCommand {

    public PingCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Ping Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to check if the bot is still alive.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`!ping` - Returns the latency of the bot.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        MessageFactory.makeInfo(message, "Pong! Time taken %s ms!", message.getJDA().getPing()).queue();
        return true;
    }
}
