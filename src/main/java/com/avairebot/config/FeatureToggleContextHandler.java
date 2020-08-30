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

package com.avairebot.config;

import com.avairebot.Constants;
import com.avairebot.contracts.toggle.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeatureToggleContextHandler {

    private static final Logger log = LoggerFactory.getLogger(FeatureToggleContextHandler.class);

    /**
     * The feature storage file, this is the file the
     * feature states will be stored to.
     */
    private static final File featureStateFile;

    /**
     * The feature storage configuration file, this is used to load/sync and save
     * the feature flag states to disk, so the states can be persisted.
     */
    private static final YamlConfiguration featureStateConfig;

    /**
     * The disabled feature holder, this holds the full enum class name
     * as the key, and a set of strings which is the name of the enum
     * instances that are disabled for that enum type.
     */
    private static HashMap<String, Set<String>> disabledFeature = new HashMap<>();

    /**
     * Creates the feature state file and config that all the feature flag
     * sates will be stored in, as-well-as syncing with the current
     * feature state file.
     */
    static {
        featureStateFile = new File(Constants.STORAGE_PATH, "feature-states.yml");
        featureStateConfig = YamlConfiguration.loadConfiguration(featureStateFile);

        for (String key : featureStateConfig.getKeys(false)) {
            disabledFeature.put(
                key.replaceAll("-", "."),
                new HashSet<>(featureStateConfig.getStringList(key))
            );
        }
    }

    /**
     * Saves the feature flag states to the storage directory so they can be
     * loaded on the next boot, persisting the states across reboots.
     */
    public static void saveToStorage() {
        try {
            for (Map.Entry<String, Set<String>> entry : disabledFeature.entrySet()) {
                featureStateConfig.set(
                    entry.getKey().replaceAll("\\.", "-"),
                    new ArrayList<>(entry.getValue())
                );
            }
            featureStateConfig.save(featureStateFile);
        } catch (IOException e) {
            log.error("Failed to save the feature flag states: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if the given feature instance type is enabled or not.
     *
     * @param instance The feature instance that should be checked.
     * @param name     The name of the type that should be checked.
     * @return {@code True} if the feature with the given name on the
     * given instance is enabled, {@code False} otherwise.
     */
    public static boolean isActive(Class<? extends Feature> instance, String name) {
        return !disabledFeature.getOrDefault(
            instance.getTypeName(),
            Collections.emptySet()
        ).contains(name);
    }

    /**
     * Enables the given feature instance type for the given name.
     *
     * @param instance The feature instance that should be enabled.
     * @param name     The name of the type that should be enabled.
     */
    public static void enable(Class<? extends Feature> instance, String name) {
        disabledFeature.getOrDefault(
            instance.getTypeName(),
            Collections.emptySet()
        ).remove(name);
    }

    /**
     * Disableds the given feature instance type for the given name.
     *
     * @param instance The feature instance that should be disabled.
     * @param name     The name of the type that should be disabled.
     */
    public static void disable(Class<? extends Feature> instance, String name) {
        String typeName = instance.getTypeName();
        if (!disabledFeature.containsKey(typeName)) {
            disabledFeature.put(typeName, new HashSet<>());
        }
        disabledFeature.get(typeName).add(name);
    }
}
