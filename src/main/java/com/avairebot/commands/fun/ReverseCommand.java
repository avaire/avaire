package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Collections;
import java.util.List;

public class ReverseCommand extends Command {

    public ReverseCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Reverse Command";
    }

    @Override
    public String getDescription() {
        return "Reverses the message given.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Reverses the given message.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command This is some random message`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("reverse");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        String string = String.join(" ", args);
        String reverse = new StringBuilder(string).reverse().toString();

        PlaceholderMessage infoMessage = context.makeInfo(reverse);

        if (string.equalsIgnoreCase(reverse)) {
            infoMessage.setFooter(context.i18n("palindrome"));
        }

        infoMessage.queue();
        return true;
    }
}
