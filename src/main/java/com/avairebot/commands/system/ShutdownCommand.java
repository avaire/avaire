package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class ShutdownCommand extends SystemCommand {

    public ShutdownCommand(AvaIre avaire) {
        super(avaire);
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
            .queue(shutdownMessage -> avaire.shutdown(), throwable -> avaire.shutdown());

        return true;
    }
}
