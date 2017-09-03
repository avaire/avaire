package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public boolean onCommand(MessageReceivedEvent e, String[] args) {
        MessageFactory.makeInfo(e.getMessage(), "Pong! Time taken %s ms!", e.getJDA().getPing()).queue();
        return true;
    }
}
