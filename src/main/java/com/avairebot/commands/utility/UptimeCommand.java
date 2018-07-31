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
import java.util.concurrent.TimeUnit;

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

        int uptimeInSeconds = (int) rb.getUptime() / 1000;
        Carbon time = Carbon.now().subSeconds(uptimeInSeconds);

        context.makeInfo(context.i18n("message"))
            .set("time", formatUptimeNicely(uptimeInSeconds))
            .setFooter(context.i18n(
                "footer",
                time.format("EEEEEEEE, dd MMM yyyy"),
                time.format("HH:mm:ss z"))
            ).queue();

        return true;
    }

    private String formatUptimeNicely(int total) {
        long days = TimeUnit.SECONDS.toDays(total);
        total -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(total);
        total -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(total);
        total -= TimeUnit.MINUTES.toSeconds(minutes);

        long seconds = TimeUnit.SECONDS.toSeconds(total);

        if (days != 0) {
            return String.format("%s, %s, %s, and %s.",
                appendIfMultiple(days, "day"),
                appendIfMultiple(hours, "hour"),
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        if (hours != 0) {
            return String.format("%s, %s, and %s.",
                appendIfMultiple(hours, "hour"),
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        if (minutes != 0) {
            return String.format("%s, and %s.",
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        return String.format("%s.", appendIfMultiple(seconds, "second"));
    }

    private String appendIfMultiple(long value, String singularType) {
        return NumberUtil.formatNicely(value) + " " + (value == 1 ? singularType : singularType + "s");
    }
}
