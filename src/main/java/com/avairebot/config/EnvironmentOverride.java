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

package com.avairebot.config;

import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

@SuppressWarnings({"WeakerAccess", "unused", "unchecked"})
public class EnvironmentOverride {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentOverride.class);

    /**
     * Overrides all the keys found in the config with their corresponding
     * environment variables, a key like "thing.stuff" would be replaced
     * with the environment variable "THING_STUFF".
     *
     * @param configuration The configuration that should have its values overridden.
     */
    public static void override(@Nonnull Configuration configuration) {
        overrideWithPrefix(null, configuration, EMPTY_LIST);
    }

    /**
     * Overrides all the keys found in the config with their corresponding
     * environment variables, a key like "thing.stuff" would be replaced
     * with the environment variable "THING_STUFF", config keys that
     * are found in the {@code protectedKeys} list will be
     * ignored and stay as they are.
     *
     * @param configuration The configuration that should have its values overridden.
     * @param protectedKeys The list of keys that should be ignored by the replacer.
     */
    public static void override(@Nonnull Configuration configuration, @Nonnull List<String> protectedKeys) {
        overrideWithPrefix(null, configuration, protectedKeys);
    }

    /**
     * Overrides all the keys found in the config with their corresponding environment
     * variables using the given prefix, a key like "thing.stuff" with a prefix of
     * "test" would be replaced with the environment variable "TEST_THING_STUFF".
     * <p>
     * Setting the {@code prefix} to {@code NULL} is the same as
     * calling the {@link #override(Configuration)} method.
     *
     * @param prefix        The prefix that should be added to all environment variables.
     * @param configuration The configuration that should have its values overridden.
     */
    public static void overrideWithPrefix(@Nullable String prefix, @Nonnull Configuration configuration) {
        overrideWithPrefix(prefix, configuration, EMPTY_LIST);
    }

    /**
     * Overrides all the keys found in the config with their corresponding environment
     * variables using the given prefix, a key like "thing.stuff" with a prefix of
     * "test" would be replaced with the environment variable "TEST_THING_STUFF",
     * config keys that are found in the {@code protectedKeys} list will be
     * ignored and stay as they are.
     * <p>
     * Setting the {@code prefix} to {@code NULL} is the same as calling
     * the {@link #override(Configuration, List)} method.
     *
     * @param prefix        The prefix that should be added to all environment variables.
     * @param configuration The configuration that should have its values overridden.
     */
    public static void overrideWithPrefix(String prefix, @Nonnull Configuration configuration, @Nonnull List<String> protectedKeys) {
        for (String key : configuration.getKeys(true)) {
            if (protectedKeys.contains(key) || configuration.isConfigurationSection(key)) {
                continue;
            }

            String environmentString = buildEnvironmentString(prefix, key);
            String env = System.getenv(environmentString);
            if (env == null) {
                continue;
            }

            setConfigurationValue(configuration, key, env);

            log.debug(
                "\"{}\" has been set to \"{}\" using the \"{}\" environment string in the \"{}\" config.",
                key, env, environmentString, configuration.getName()
            );
        }
    }

    private static void setConfigurationValue(Configuration configuration, String key, String value) {
        if (configuration.isBoolean(key)) {
            configuration.set(key, ComparatorUtil.getFuzzyType(value).getValue());
        } else if (configuration.isInt(key)) {
            configuration.set(key, NumberUtil.parseInt(value, configuration.getInt(key)));
        } else if (configuration.isLong(key)) {
            try {
                configuration.set(key, Long.parseLong(value));
            } catch (NumberFormatException ignored) {

            }
        } else if (configuration.isList(key)) {
            configuration.set(key, convertStringToList(value));
        } else {
            configuration.set(key, value);
        }
    }

    private static List<String> convertStringToList(String value) {
        return Arrays.stream(value.split(";"))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private static String buildEnvironmentString(@Nullable String prefix, String key) {
        String env = String.join("_", key.split("\\."))
            .replaceAll("-", "_")
            .toUpperCase();

        if (prefix == null) {
            return env;
        }
        return prefix.toUpperCase() + "_" + env;
    }
}
