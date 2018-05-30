package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
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
    public void onIntent(CommandMessage context, AIResponse response) {
        String helpCommand = ".help";
        if (context.getMessage().getChannelType().isGuild()) {
            HelpCommand command = getHelpCommand();
            if (command != null) {
                helpCommand = command.generateCommandTrigger(context.getMessage());
            }
        }

        context.makeWarning(
            StringReplacementUtil.replaceAll(
                response.getResult().getFulfillment().getSpeech(),
                "!help", helpCommand
            )
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
