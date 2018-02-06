package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.fun.RandomCatCommand;
import com.avairebot.contracts.ai.Intent;
import net.dv8tion.jda.core.entities.Message;

public class RequestCat extends Intent {

    public RequestCat(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.cat";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(Message message, AIResponse response) {
        CommandHandler.getCommand(RandomCatCommand.class)
            .getCommand().onCommand(new CommandMessage(message), new String[0]);
    }
}
