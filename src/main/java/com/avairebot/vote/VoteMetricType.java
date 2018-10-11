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

package com.avairebot.vote;

public enum VoteMetricType {

    /**
     * Represents votes received through the DBL webhook.
     */
    WEBHOOK("Webhook"),

    /**
     * Represents votes that was validated through the <code>!vote check</code> command.
     */
    COMMAND("Command");

    private final String name;

    VoteMetricType(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the vote metric type.
     *
     * @return The name of the metric type.
     */
    public String getName() {
        return name;
    }
}
