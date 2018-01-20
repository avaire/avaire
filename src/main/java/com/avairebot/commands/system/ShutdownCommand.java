package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shared.ExitCodes;
import com.avairebot.time.Carbon;
import com.avairebot.time.Formats;
import net.dv8tion.jda.core.entities.Message;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShutdownCommand extends SystemCommand {

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
    public boolean onCommand(Message message, String[] args) {
        if (message instanceof CommandMessage) {
            CommandMessage commandMessage = (CommandMessage) message;

            if (commandMessage.isMentionableCommand()) {
                return sendErrorMessage(message, "This command can not be used via mentions!");
            }
        }

        if (args.length == 0) {
            return sendErrorMessage(message, "You must include the time you want the bot to shutdown.");
        }

        if (args[0].equalsIgnoreCase("now")) {
            MessageFactory.makeInfo(message, "Shutting down processes... See you soon :wave:")
                .queue(shutdownMessage -> avaire.shutdown(), throwable -> avaire.shutdown());

            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            MessageFactory.makeInfo(message, "The shutdown process has been canceled.")
                .queue(
                    shutdownMessage -> avaire.scheduleShutdown(null, ExitCodes.EXIT_CODE_RESTART),
                    throwable -> avaire.scheduleShutdown(null, ExitCodes.EXIT_CODE_RESTART)
                );

            return true;
        }

        Carbon time = formatInput(String.join(" ", args));
        if (time == null) {
            return sendErrorMessage(message, "Invalid time format given, `%s` is not a valid supported time format.",
                String.join(" ", args)
            );
        }

        if (time.isPast()) {
            return sendErrorMessage(message, "The time given is in the past, that doesn't really work... Use a time set in the future, or use `now`.");
        }

        MessageFactory.makeSuccess(message, "The bot has been scheduled to restart in :fromNow.\n**Date:** :date")
            .set("fromNow", time.diffForHumans(true))
            .set("date", time.format("EEEEEEEE, dd MMMMMMM yyyy - HH:mm:ss z"))
            .queue(
                shutdownMessage -> avaire.scheduleShutdown(time, ExitCodes.EXIT_CODE_RESTART),
                throwable -> avaire.scheduleShutdown(time, ExitCodes.EXIT_CODE_RESTART)
            );

        return true;
    }

    private Carbon formatInput(String time) {
        for (Formats format : Formats.values()) {
            try {
                return Carbon.createFromFormat(format.getFormat(), time);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
