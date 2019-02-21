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

package com.avairebot.contracts.metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public interface HasMetadata<T, V> {

    /**
     * Sets the metadata key for the object, setting the key to the given value,
     * if {@code NULL} is given, the key is instead removed from the metadata.
     *
     * @param key   The key that the value should be stored under.
     * @param value The value that should be stored.
     */
    void setMetadata(@Nonnull String key, @Nullable Object value);

    /**
     * Removes the given key from the metadata if it exists.
     *
     * @param key The key that should be removed from the metadata.
     */
    void removeMetadata(@Nonnull String key);

    /**
     * Returns the full metadata object, or {@code NULL} if
     * nothing has been saved to the metadata object yet.
     *
     * @return Possibly-null, the full metadata object.
     */
    @Nullable
    HashMap<T, V> getMetadata();

    /**
     * Gets the value of the given key from the metadata object,
     * if the key doesn't exist in the metadata object,
     * {@code NULL} will be returned instead.
     *
     * @param key The key that should have its value returned.
     * @return Possibly-null, the value of the given key.
     */
    @Nullable
    V getMetadataFromKey(@Nonnull String key);

    /**
     * Checks if the given key exists in the metadata object.
     *
     * @param key The key that should be checked exists.
     * @return {@code True} if the key exists, {@code False} otherwise.
     */
    boolean hasMetadataKey(@Nonnull String key);
}
