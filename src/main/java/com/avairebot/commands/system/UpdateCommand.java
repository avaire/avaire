package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.ApplicationShutdownCommand;
import com.avairebot.shared.ExitCodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpdateCommand extends ApplicationShutdownCommand {

    public UpdateCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Update Command";
    }

    @Override
    public String getDescription() {
        return "Schedule a time the bot should be automatically-updated, the bot will shutdown, update itself, and start back up again.\nThis requires [avaire/watchdog](https://github.com/avaire/watchdog) to work, or that the `--internal-restart` flag was used when starting the bot, without it the bot will just shutdown.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command now` - Updates the bot now.",
            "`:command cancel` - Cancels the update process.",
            "`:command <time>` - Schedules a time the bot should be updated."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("update");
    }

    @Override
    public int exitCode() {
        return ExitCodes.EXIT_CODE_UPDATE;
    }

    @Override
    public String shutdownNow() {
        return "Shutting down processes and updating the application... See you soon :wave:";
    }

    @Override
    public String scheduleShutdown() {
        return "The bot has been scheduled to be updated in :fromNow.\n**Date:** :date";
    }

    @Override
    public String scheduleCancel() {
        return "The update process has been canceled.";
    }
}
