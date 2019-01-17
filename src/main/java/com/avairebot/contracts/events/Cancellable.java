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

package com.avairebot.contracts.events;

public interface Cancellable {

    /**
     * Gets the cancellation state of this event. A cancelled event will
     * not be executed, but will still pass to other plugins.
     *
     * @return {@code True} if this event is cancelled.
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of this event. A cancelled event will
     * not be executed, but will still pass to other plugins.
     *
     * @param cancel {@code True} if you wish to cancel this event.
     */
    void setCancelled(boolean cancel);
}
