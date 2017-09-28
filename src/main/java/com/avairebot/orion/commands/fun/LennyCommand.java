package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LennyCommand extends AbstractCommand {

    private final Random random = new Random();

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
    public String getExampleUsage() {
        return "`:command`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lenny");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (random.nextInt(25) > 0) {
            message.getChannel().sendMessage("( ͡° ͜ʖ ͡°)").queue();
            return true;
        }

        message.getChannel().sendMessage("( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ( ͡° ͜ʖ ͡°) ͜ʖ ͡°)ʖ ͡°)ʖ ͡°)").queue();
        return true;
    }
}
