package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoteCommand extends Command {

    public VoteCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Vote Command";
    }

    @Override
    public String getDescription() {
        return "Enjoy using the bot? Consider voting for the bot to help it grow, it's free but means a lot to the team behind Ava <3";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("vote");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.makeSuccess(String.join("\n", Arrays.asList(
            "Enjoy using the bot? Consider voting for the bot to help it grow, it's free but means a lot to the team behind Ava ❤",
            "",
            "https://discordbots.org/bot/avaire",
            "",
            "Rewards for voting will be coming soon!"
        )))
            .setTitle("Vote for AvaIre on DBL", "https://discordbots.org/bot/avaire")
            .queue();

        return true;
    }
}
