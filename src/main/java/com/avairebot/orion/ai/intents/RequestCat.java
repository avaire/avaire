package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.fun.RandomCatCommand;
import com.avairebot.orion.contracts.ai.Intent;
import net.dv8tion.jda.core.entities.Message;

public class RequestCat extends Intent {

    public RequestCat(Orion orion) {
        super(orion);
    }

    @Override
    public String getAction() {
        return "request.cat";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        CommandContainer command = CommandHandler.getCommand(RandomCatCommand.class);

        if (command == null) {
            return;
        }

        command.getCommand().onCommand(message, new String[0]);
    }
}
