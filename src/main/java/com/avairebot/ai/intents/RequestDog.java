package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.fun.RandomDogCommand;
import com.avairebot.contracts.ai.Intent;

public class RequestDog extends Intent {

    public RequestDog(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.dog";
    }

    @Override
    @SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
    public void onIntent(CommandMessage context, AIResponse response) {
        CommandHandler.getCommand(RandomDogCommand.class)
            .getCommand().onCommand(new CommandMessage(context), new String[0]);
    }
}
