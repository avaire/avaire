package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;

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
        return "Uptime Command";
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
    public boolean onCommand(CommandMessage context, String[] args) {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

        Carbon time = Carbon.now().subSeconds(NumberUtil.parseInt("" + (rb.getUptime() / 1000)));

        context.makeInfo(context.i18n("message"))
            .set("time", time.diffForHumans(true))
            .setFooter(String.format(
                context.i18n("footer"),
                time.format("EEEEEEEE, dd MMM yyyy"),
                time.format("HH:mm:ss z")
            ))
            .queue();

        return true;
    }
}
