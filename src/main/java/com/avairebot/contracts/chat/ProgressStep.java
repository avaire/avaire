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

public class ProgressStep {

    private final String message;
    private final String failureMessage;
    private final ProgressClosure closure;

    private boolean completed = false;
    private ProgressStepStatus status = ProgressStepStatus.WAITING;

    public ProgressStep(String message, ProgressClosure closure, String failureMessage) {
        this.message = message;
        this.closure = closure;
        this.failureMessage = failureMessage;
    }

    public boolean run() {
        try {
            status = closure.run()
                ? ProgressStepStatus.SUCCESS
                : ProgressStepStatus.FAILURE;
        } catch (Exception e) {
            status = ProgressStepStatus.FAILURE;
        }

        completed = true;

        return status.getValue();
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getMessage() {
        return message;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public ProgressStepStatus getStatus() {
        return status;
    }
}
