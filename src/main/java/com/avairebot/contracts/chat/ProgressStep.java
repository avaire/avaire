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

import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

public class ProgressStep {

    private final String message;
    private final String failureMessage;
    private final ProgressClosure closure;
    private FriendlyException exception;

    private boolean completed = false;
    private ProgressStepStatus status = ProgressStepStatus.WAITING;

    /**
     * Creates a new progress step instance using the given
     * message, closure, and failure fallback message.
     *
     * @param message        The message that should be used for the progress step.
     * @param closure        The closure that should handle the progress step task.
     * @param failureMessage The failure message that should be displayed in the event
     *                       the closure fails or throws an exception.
     */
    public ProgressStep(String message, ProgressClosure closure, String failureMessage) {
        this.message = message;
        this.closure = closure;
        this.failureMessage = failureMessage;
    }

    /**
     * Runs the progress step task, invoking the {@link #closure closure}.
     *
     * @return {@code True} if the progress closure ran successfully, {@code False} otherwise.
     * @throws FriendlyException Thrown if progress closure threw an exception.
     */
    public boolean run() throws FriendlyException {
        completed = true;

        try {
            status = closure.run()
                ? ProgressStepStatus.SUCCESS
                : ProgressStepStatus.FAILURE;
        } catch (Exception e) {
            status = ProgressStepStatus.FAILURE;

            throw ExceptionTools.wrapUnfriendlyExceptions(
                e.getMessage(), FriendlyException.Severity.COMMON, e
            );
        }

        return status.getValue();
    }

    /**
     * Checks if the progress step have already been invoked.
     *
     * @return {@code True} if the task have already ran.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Gets the message associated with the progress step task.
     *
     * @return The message describing the progress step task.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the failure message, the message is only shown in the
     * event the {@link #closure} fails to run successfully.
     *
     * @return The failure message.
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Gets the exception that the progress step task failed with.
     *
     * @return The exception the progress step task failed with, or {@code NULL}
     *         if the task haven't yet been run, or didn't fail when it did run.
     */
    public FriendlyException getException() {
        return exception;
    }

    /**
     * Sets the exception that the progress step task failed with.
     *
     * @param exception The exception that the progress step task have failed with.
     */
    public void setException(FriendlyException exception) {
        this.exception = exception;
    }

    /**
     * Gets the progress step status for the current step task.
     *
     * @return The progress step status for the current task.
     */
    public ProgressStepStatus getStatus() {
        return status;
    }
}
