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

package com.avairebot.handlers.events;

import com.avairebot.shared.ExitCodes;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nonnull;

public class ApplicationShutdownEvent extends Event {

    private final ShutdownReason reason;

    /**
     * Creates a new application shutdown event, this is called just before
     * the application is about to shutdown completely.
     *
     * @param api      The JDA api(shard) that the event should be triggered on.
     * @param exitCode The exit code that the application is closing with.
     */
    public ApplicationShutdownEvent(JDA api, int exitCode) {
        super(api);

        reason = ShutdownReason.fromExitCode(exitCode);
    }

    /**
     * Gets the reason for the shutdown.
     *
     * @return The reason for the shutdown.
     */
    public ShutdownReason getReason() {
        return reason;
    }

    /**
     * Gets the exit code that the application will shutdown with.
     *
     * @return The exit code that the application will shutdown with.
     */
    public int getExitCode() {
        return reason.getExitCode();
    }

    /**
     * The shutdown reasons available for the
     * different supported exit codes.
     */
    public enum ShutdownReason {

        NORMAL(ExitCodes.EXIT_CODE_NORMAL),
        ERROR(ExitCodes.EXIT_CODE_ERROR),
        RESTART(ExitCodes.EXIT_CODE_RESTART),
        UPDATE(ExitCodes.EXIT_CODE_UPDATE),
        UNKNOWN(-1);

        private final int exitCode;

        ShutdownReason(int exitCode) {
            this.exitCode = exitCode;
        }

        /**
         * Gets the shutdown reason from the given exit code.
         *
         * @param exitCode The exit code the shutdown reason should match with.
         * @return The matching shutdown reason to the given exit code,
         *         or {@link #UNKNOWN} if there were no match.
         */
        @Nonnull
        public static ShutdownReason fromExitCode(int exitCode) {
            for (ShutdownReason reason : values()) {
                if (reason.getExitCode() == exitCode) {
                    return reason;
                }
            }
            return ShutdownReason.UNKNOWN;
        }

        /**
         * Gets the shutdown reason exit code.
         *
         * @return The shutdown reason exit code.
         */
        public int getExitCode() {
            return exitCode;
        }
    }
}
