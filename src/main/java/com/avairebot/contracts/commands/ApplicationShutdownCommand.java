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

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.shared.ExitCodes;
import com.avairebot.time.Carbon;
import com.avairebot.time.Formats;

import java.text.ParseException;

public abstract class ApplicationShutdownCommand extends SystemCommand {

    /**
     * Creates the given command instance by calling {@link #ApplicationShutdownCommand(AvaIre, boolean)} with allowDM set to true.
     *
     * @param avaire The AvaIre class instance.
     */
    public ApplicationShutdownCommand(AvaIre avaire) {
        this(avaire, true);
    }

    /**
     * Creates the given command instance with the given
     * AvaIre instance and the allowDM settings.
     *
     * @param avaire  The AvaIre class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public ApplicationShutdownCommand(AvaIre avaire, boolean allowDM) {
        super(avaire, allowDM);
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The JDA message object from the message received event.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.isMentionableCommand()) {
            return sendErrorMessage(context, "This command can not be used via mentions!");
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "You must include the time you want the bot to shutdown.");
        }

        if (args[0].equalsIgnoreCase("now")) {
            context.makeInfo(shutdownNow())
                .queue(
                    shutdownMessage -> avaire.shutdown(exitCode()),
                    throwable -> avaire.shutdown(exitCode())
                );

            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            context.makeInfo(scheduleCancel())
                .queue(
                    shutdownMessage -> avaire.scheduleShutdown(null, exitCode()),
                    throwable -> avaire.scheduleShutdown(null, exitCode())
                );

            return true;
        }

        Carbon time = formatInput(String.join(" ", args));
        if (time == null) {
            return sendErrorMessage(context, "Invalid time format given, `%s` is not a valid supported time format.",
                String.join(" ", args)
            );
        }

        if (time.isPast()) {
            return sendErrorMessage(context, "The time given is in the past, that doesn't really work... Use a time set in the future, or use `now`.");
        }

        context.makeSuccess(scheduleShutdown())
            .set("fromNow", time.diffForHumans(true))
            .set("date", time.format("EEEEEEEE, dd MMMMMMM yyyy - HH:mm:ss z"))
            .queue(
                shutdownMessage -> avaire.scheduleShutdown(time, exitCode()),
                throwable -> avaire.scheduleShutdown(time, exitCode())
            );

        return true;
    }

    /**
     * Formats the input given by the user, trying to parse the time
     * to any of the valid formats supported by {@link Carbon}.
     *
     * @param time The string version of the time.
     * @return Possibly-null, A Carbon instance matching the given time, or {@code null}.
     */
    private Carbon formatInput(String time) {
        for (Formats format : Formats.values()) {
            try {
                return Carbon.createFromFormat(format.getFormat(), time);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /**
     * The {@link ExitCodes} that should be used when the bot shuts down.
     *
     * @return The exit code that should be used when the bot shuts down.
     */
    public abstract int exitCode();

    /**
     * The message that should be sent when the bot shuts down right now.
     *
     * @return The message that should be sent when the bot shuts down right now.
     */
    public abstract String shutdownNow();

    /**
     * The message that should be sent when the bot is scheduled to shutdown.
     *
     * @return The message that should be sent when the bot is scheduled to shutdown.
     */
    public abstract String scheduleShutdown();

    /**
     * The message that should be sent when a scheduled shutdown process is canceled.
     *
     * @return The message that should be sent when a scheduled shutdown process is canceled.
     */
    public abstract String scheduleCancel();
}
