package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RandomUtil;

import java.util.Collections;
import java.util.List;

public class LennyCommand extends Command {

    public LennyCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Lenny command";
    }

    @Override
    public String getDescription() {
        return "( ͡° ͜ʖ ͡°)";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - ( ͡° ͜ʖ ͡°)");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lenny");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (RandomUtil.getInteger(25) > 0) {
            context.getMessageChannel().sendMessage("( ͡° ͜ʖ ͡°)").queue();
            return true;
        }

        context.getMessageChannel().sendMessage("( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ͡°) ͜ʖ ͡°)ʖ ͡°)ʖ ͡°)").queue();
        return true;
    }
}
