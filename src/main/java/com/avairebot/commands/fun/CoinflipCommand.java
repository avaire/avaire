package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RandomUtil;

import java.util.Arrays;
import java.util.List;

public class CoinflipCommand extends Command {

    private final String heads = "https://cdn.discordapp.com/attachments/279462105277530112/279614727406223360/Heads.png";
    private final String tails = "https://cdn.discordapp.com/attachments/279462105277530112/279614727431258112/Tails.png";

    public CoinflipCommand(AvaIre avaire) {
        super(avaire);
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
    public List<String> getTriggers() {
        return Arrays.asList("coinflip", "coin");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.getMessageChannel().sendMessage(RandomUtil.getBoolean() ? heads : tails).queue();

        return false;
    }
}
