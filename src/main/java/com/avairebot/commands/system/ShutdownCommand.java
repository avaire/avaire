package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.ApplicationShutdownCommand;
import com.avairebot.shared.ExitCodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShutdownCommand extends ApplicationShutdownCommand {

    public ShutdownCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Shutdown Command";
    }

    @Override
    public String getDescription() {
        return "Schedules a time the bot should be shutdown gracefully.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command now` - Shuts down the bot now.",
            "`:command cancel` - Cancels the shutdown process.",
            "`:command <time>` - Schedules a time the bot should be shutdown."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("shutdown");
    }

    @Override
    public int exitCode() {
        return ExitCodes.EXIT_CODE_NORMAL;
    }

    @Override
    public String shutdownNow() {
        return "Shutting down processes... See you soon :wave:";
    }

    @Override
    public String scheduleShutdown() {
        return "The bot has been scheduled to shutdown in :fromNow.\n**Date:** :date";
    }

    @Override
    public String scheduleCancel() {
        return "The shutdown process has been canceled.";
    }
}
