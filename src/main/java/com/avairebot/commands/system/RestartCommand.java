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
        return "Schedule a time the bot should be automatically-restarted, the bot will shutdown, then start back up again.\nThis requires [avaire/watchdog](https://github.com/avaire/watchdog) to work, or that the `--internal-restart` flag was used when starting the bot, without it the bot will just shutdown.";
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
