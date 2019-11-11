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

    SUCCESS("\u2611", true),
    FAILURE("\u274c", false),
    WAITING("\u27a1"),
    RUNNING("\uD83D\uDD04");

    private final String defaultEmote;
    private final boolean value;

    ProgressStepStatus(String defaultEmote, boolean value) {
        this.defaultEmote = defaultEmote;
        this.value = value;
    }

    ProgressStepStatus(String defaultEmote) {
        this(defaultEmote, false);
    }

    public String getDefaultEmote() {
        return defaultEmote;
    }

    public boolean getValue() {
        return value;
    }
}
