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

package com.avairebot.contracts.toggle;

import com.avairebot.config.FeatureToggleContextHandler;

public interface Feature {

    /**
     * The name of the feature.
     * <p>
     * The feature interface is meant to be used with enum instances,
     * which already implement a name method that's unique to each
     * enum, this implementation means that enums that want to
     * use the feature interface only have to implement it,
     * and it's already setup for them.
     *
     * @return Name of the feature.
     */
    String name();

    /**
     * Checks if the current instance is enabled.
     *
     * @return {@code True} if the feature is enabled,
     *         {@code False} otherwise.
     */
    default boolean isActive() {
        return FeatureToggleContextHandler.isActive(getClass(), name());
    }

    /**
     * Enables the current instance.
     */
    default void enable() {
        FeatureToggleContextHandler.enable(getClass(), name());
    }

    /**
     * Disables the current instance.
     */
    default void disable() {
        FeatureToggleContextHandler.disable(getClass(), name());
    }
}
