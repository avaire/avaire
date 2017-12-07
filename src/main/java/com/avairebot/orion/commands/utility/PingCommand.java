package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;

public class PingCommand extends Command {

    public PingCommand(Orion orion) {
        super(orion);
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
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        long start = System.currentTimeMillis();
        message.getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - start;

            MessageFactory.makeInfo(message, "Pong! Time taken :ping ms (:rating) Websocket heartbeat :heartbeat ms")
                .set("heartbeat", message.getJDA().getPing())
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
