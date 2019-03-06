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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.ApplicationShutdownCommand;
import com.avairebot.shared.ExitCodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestartCommand extends ApplicationShutdownCommand {

    public RestartCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Restart Command";
    }

    @Override
    public String getDescription() {
        return "Schedule a time the bot should be automatically-restarted, the bot will shutdown, then start back up again.\nThis requires [avaire/watchdog](https://github.com/avaire/watchdog) to work, without it the bot will just shutdown.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command now` - Restarts the bot now.",
            "`:command cancel` - Cancels the restart process.",
            "`:command <time>` - Schedules a time the bot should be restarted."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("restart");
    }

    @Override
    public int exitCode() {
        return ExitCodes.EXIT_CODE_RESTART;
    }

    @Override
    public String shutdownNow() {
        return "Shutting down processes and restarting... See you soon :wave:";
    }

    @Override
    public String scheduleShutdown() {
        return "The bot has been scheduled to be restarted in :fromNow.\n**Date:** :date";
    }

    @Override
    public String scheduleCancel() {
        return "The restart process has been canceled.";
    }
}
