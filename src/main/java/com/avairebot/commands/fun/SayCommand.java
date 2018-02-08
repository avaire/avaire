package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Collections;
import java.util.List;

public class SayCommand extends Command {

    public SayCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Say Command";
    }

    @Override
    public String getDescription() {
        return "I will say whatever you tell me to.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Makes the bot say the given message");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command I am a BOT`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("say");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,text.manage_messages");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing `message` argument, the `message` argument is required!");
        }

        context.getMessageChannel().sendMessage(context.getContentRaw()).queue();

        if (context.isGuildMessage()) {
            context.delete().reason("AvaIre say command usage").queue();
        }

        return true;
    }
}
