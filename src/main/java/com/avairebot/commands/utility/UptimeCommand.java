/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;
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
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Displays how long the bot has been online for.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("uptime");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.BOT_INFORMATION);
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

    public String formatUptimeNicely(int total) {
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
