package com.avairebot.orion.commands.system;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandMessage;
import com.avairebot.orion.contracts.commands.SystemCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class ShutdownCommand extends SystemCommand {

    public ShutdownCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Shutdown Command";
    }

    @Override
    public String getDescription() {
        return "This command shuts down all instances of the bot gracefully.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("shutdown");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (message instanceof CommandMessage) {
            CommandMessage commandMessage = (CommandMessage) message;

            if (commandMessage.isMentionableCommand()) {
                return sendErrorMessage(message, "This command can not be used via mentions!");
            }
        }

        MessageFactory.makeInfo(message, "Shutting down processes... See you soon :wave:")
            .queue(shutdownMessage -> orion.shutdown(), throwable -> orion.shutdown());

        return true;
    }
}
