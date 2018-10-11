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

package com.avairebot.cache;

import com.avairebot.cache.adapters.FileAdapter;
import com.avairebot.cache.adapters.MemoryAdapter;
import com.avairebot.contracts.cache.CacheAdapter;
import com.avairebot.shared.ExitCodes;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

public enum CacheType {

    /**
     * Represents a file cache type, can be used to store
     * things that are persisted throughout restarts and
     * for very long periods of time in general.
     */
    FILE("File", false, FileAdapter.class),

    /**
     * Represents a memory cache type, can be used to store
     * things directly in the memory, is great for storing
     * something that may have to be accessed a lot,
     * or things that has a short lifespan.
     */
    MEMORY("Memory", true, MemoryAdapter.class);

    private static final EnumMap<CacheType, CacheAdapter> INSTANCES = new EnumMap<>(CacheType.class);

    static {
        for (CacheType type : values()) {
            try {
                INSTANCES.put(type, type.getClassInstance().getDeclaredConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.printf("Invalid cache type given: %s", e.getMessage());
                System.exit(ExitCodes.EXIT_CODE_ERROR);
            }
        }
    }

    private final String name;
    private final boolean isDefault;
    private final Class<? extends CacheAdapter> instance;

    CacheType(String name, boolean isDefault, Class<? extends CacheAdapter> instance) {
        this.name = name;
        this.isDefault = isDefault;
        this.instance = instance;
    }

    /**
     * Gets the default cache type.
     *
     * @return The default cache type.
     */
    public static CacheType getDefault() {
        return MEMORY;
    }

    /**
     * Gets the cache type by name.
     *
     * @param name The name of the cache type that should be returned.
     * @return Possibly-null, the cache type with the given name.
     */
    @Nullable
    public static CacheType fromName(String name) {
        for (CacheType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the name of the cache type.
     *
     * @return The name of the cache type.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the current cache type is the default cache type.
     *
     * @return <code>True</code> if the cache type is the default cache type, or <code>False</code> otherwise.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Gets the cache adapter class instance.
     *
     * @return The cache adapter class instance.
     */
    public Class<? extends CacheAdapter> getClassInstance() {
        return instance;
    }

    /**
     * Gets the adapter instance for the current cache type.
     *
     * @return The adapter instance for the current cache type.
     */
    public CacheAdapter getAdapter() {
        return INSTANCES.get(this);
    }
}
