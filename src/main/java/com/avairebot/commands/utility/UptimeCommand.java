package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;

public class UptimeCommand extends Command {

    public UptimeCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Uptime Comand";
    }

    @Override
    public String getDescription() {
        return "Displays how long the bot has been online for.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("uptime");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

        Carbon time = Carbon.now().subSeconds(NumberUtil.parseInt("" + (rb.getUptime() / 1000)));

        MessageFactory.makeInfo(message, "I have been online for :time")
            .set("time", time.diffForHumans(true))
            .setFooter("Started " + time.format("EEEEEEEE, dd MMM yyyy") + " at " + time.format("HH:mm:ss z"))
            .queue();

        return true;
    }
}
