package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CoinflipCommand extends AbstractCommand {

    private final Random random = new Random();
    private final String heads = "https://cdn.discordapp.com/attachments/279462105277530112/279614727406223360/Heads.png";
    private final String tails = "https://cdn.discordapp.com/attachments/279462105277530112/279614727431258112/Tails.png";

    public CoinflipCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Coinflip Command";
    }

    @Override
    public String getDescription() {
        return "Flips a coin heads or tails.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("coinflip", "coin");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        message.getChannel().sendMessage(random.nextInt(1000000) < 500000 ? heads : tails).queue();

        return false;
    }
}
