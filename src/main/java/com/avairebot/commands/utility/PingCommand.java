package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Collections;
import java.util.List;

public class PingCommand extends Command {

    public PingCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Ping Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to check if the bot is still alive.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Returns the latency of the bot.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        long start = System.currentTimeMillis();
        context.getMessage().getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - start;

            context.makeInfo("Pong! Time taken :ping ms (:rating) Websocket heartbeat :heartbeat ms")
                .set("heartbeat", context.getJDA().getPing())
                .set("rating", ratePing(ping))
                .set("ping", ping)
                .queue();

        });
        return true;
    }

    private String ratePing(long ping) {
        if (ping <= 10) return "faster than Sonic! :smiley_cat:";
        if (ping <= 100) return "great! :smiley:";
        if (ping <= 200) return "nice! :slight_smile:";
        if (ping <= 300) return "decent. :neutral_face:";
        if (ping <= 400) return "average... :confused:";
        if (ping <= 500) return "slightly slow. :slight_frown:";
        if (ping <= 600) return "kinda slow.. :frowning2:";
        if (ping <= 700) return "slow.. :worried:";
        if (ping <= 800) return "too slow. :disappointed:";
        if (ping <= 900) return "bad. :sob: (helpme)";
        if (ping <= 1600) return "#BlameDiscord. :angry:";
        if (ping <= 10000) return "this makes no sense :thinking: #BlameAlexis";
        return "slow af. :dizzy_face: ";
    }
}
