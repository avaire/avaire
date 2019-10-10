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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public interface Feature {

    /**
     * The disabled feature holder, this holds the full enum class name
     * as the key, and a set of strings which is the name of the enum
     * instances that are disabled for that enum type.
     */
    HashMap<String, Set<String>> disabledFeature = new HashMap<>();

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
        return !disabledFeature.getOrDefault(
            this.getClass().getTypeName(),
            Collections.emptySet()
        ).contains(name());
    }

    /**
     * Enables the current instance.
     */
    default void enable() {
        disabledFeature.getOrDefault(
            this.getClass().getTypeName(),
            Collections.emptySet()
        ).remove(name());
    }

    /**
     * Disables the current instance.
     */
    default void disable() {
        String typeName = this.getClass().getTypeName();
        if (!disabledFeature.containsKey(typeName)) {
            disabledFeature.put(typeName, new HashSet<>());
        }
        disabledFeature.get(typeName).add(name());
    }
}
