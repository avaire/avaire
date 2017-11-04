package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.help.HelpCommand;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class Unknown extends Intent {

    public Unknown(Orion orion) {
        super(orion);
    }

    @Override
    public String getAction() {
        return "input.unknown";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        String helpCommand = ".help";
        if (message.getChannelType().isGuild()) {
            HelpCommand command = getHelpCommand();
            if (command != null) {
                helpCommand = command.generateCommandTrigger(message);
            }
        }

        MessageFactory.makeWarning(message,
            response.getResult().getFulfillment().getSpeech().replaceAll(".help", helpCommand)
        ).queue();
    }

    private HelpCommand getHelpCommand() {
        for (CommandContainer container : CommandHandler.getCommands()) {
            if (container.getCommand() instanceof HelpCommand) {
                return (HelpCommand) container.getCommand();
            }
        }
        return null;
    }
}
