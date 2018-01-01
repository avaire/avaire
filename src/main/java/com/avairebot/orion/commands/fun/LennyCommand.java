package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.utilities.RandomUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class LennyCommand extends Command {

    public LennyCommand(Orion orion) {
        super(orion);
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
    public boolean onCommand(Message message, String[] args) {
        if (RandomUtil.getInteger(25) > 0) {
            message.getChannel().sendMessage("( ͡° ͜ʖ ͡°)").queue();
            return true;
        }

        message.getChannel().sendMessage("( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ͡°) ͜ʖ ͡°)ʖ ͡°)ʖ ͡°)").queue();
        return true;
    }
}
