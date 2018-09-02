package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.help.HelpCommand;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.utilities.StringReplacementUtil;

public class Unknown extends Intent {

    public Unknown(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "input.unknown";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(CommandMessage context, AIResponse response) {
        context.makeWarning(
            StringReplacementUtil.replaceAll(
                response.getResult().getFulfillment().getSpeech(),
                "!help", CommandHandler.getCommand(HelpCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            )
        ).queue();
    }
}
