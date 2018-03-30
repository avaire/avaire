package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RepeatCommand extends Command {

    public RepeatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Repeat Command";
    }

    @Override
    public String getDescription() {
        return "I will repeat anything you say.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Repeats the given message");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command I am a BOT`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("repeat", "echo");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        context.getMessageChannel().sendMessage(context.getContentRaw()).queue();

        return true;
    }
}
