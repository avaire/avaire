package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.fun.RandomDogCommand;
import com.avairebot.contracts.ai.Intent;
import net.dv8tion.jda.core.entities.Message;

public class RequestDog extends Intent {

    public RequestDog(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.dog";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        CommandContainer command = CommandHandler.getCommand(RandomDogCommand.class);

        if (command == null) {
            return;
        }

        command.getCommand().onCommand(message, new String[0]);
    }
}
