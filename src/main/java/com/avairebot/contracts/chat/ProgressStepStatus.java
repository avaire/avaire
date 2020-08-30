/*
 * Copyright (c) 2019.
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

package com.avairebot.contracts.chat;

public enum ProgressStepStatus {

    /**
     * Success, this is used when a progress step task
     * have finished successfully with no errors.
     */
    SUCCESS("\u2611", true),

    /**
     * Failure, this is used when a progress step task have finished, but
     * returned false as a result, or threw an error during its process.
     */
    FAILURE("\u274c", false),

    /**
     * Waiting, this represents a progress step task that have
     * yet to run, and is waiting to be invoked.
     */
    WAITING("\u27a1"),

    /**
     * Running, this represents a progress step task that is currently running.
     */
    RUNNING("\uD83D\uDD04");

    private final String defaultEmote;
    private final boolean value;

    /**
     * Creates a new progress step status instance with the given default
     * emote, and the given value representing the status.
     *
     * @param defaultEmote The emote that should be used by default
     *                     for the progress step status.
     * @param value        The value that represents the progress status status.
     */
    ProgressStepStatus(String defaultEmote, boolean value) {
        this.defaultEmote = defaultEmote;
        this.value = value;
    }

    /**
     * Creates a new progress step status instance with the given
     * default emote, and with a default value of {@code False}.
     *
     * @param defaultEmote The emote that should be used by default
     *                     for the progress step status.
     */
    ProgressStepStatus(String defaultEmote) {
        this(defaultEmote, false);
    }

    /**
     * Gets the default emote that should be used with
     * the current progress step status.
     *
     * @return The default emote that should be used.
     */
    public String getDefaultEmote() {
        return defaultEmote;
    }

    /**
     * Gets the value that is used with the current progress step status.
     *
     * @return The boolean value that represents the status
     * of the current progress step status.
     */
    public boolean getValue() {
        return value;
    }
}
