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

            context.makeInfo(context.i18n("message"))
                .set("heartbeat", context.getJDA().getPing())
                .set("rating", ratePing(context, ping))
                .set("ping", ping)
                .queue();
        });
        return true;
    }

    private String ratePing(CommandMessage context, long ping) {
        if (ping <= 10) return context.i18n("rating.10");
        if (ping <= 100) return context.i18n("rating.100");
        if (ping <= 200) return context.i18n("rating.200");
        if (ping <= 300) return context.i18n("rating.300");
        if (ping <= 400) return context.i18n("rating.400");
        if (ping <= 500) return context.i18n("rating.500");
        if (ping <= 600) return context.i18n("rating.600");
        if (ping <= 700) return context.i18n("rating.700");
        if (ping <= 800) return context.i18n("rating.800");
        if (ping <= 900) return context.i18n("rating.900");
        if (ping <= 1600) return context.i18n("rating.1600");
        if (ping <= 10000) return context.i18n("rating.10000");
        return context.i18n("rating.other");
    }
}
