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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentMacros {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentMacros.class);
    private static final Pattern jawsDBUrlPattern = Pattern.compile(
        "^(mysql:\\/\\/+([A-Za-z0-9]{4,})+\\:+([A-Za-z0-9]{4,})+\\@+([A-Za-z0-9\\-\\.\\_]{4,}+\\:+[0-9]{2,6})\\/+([A-Za-z0-9\\-\\_\\.]{4,}))$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Registers the default environment override macros.
     */
    public static void registerDefaults() {
        if (!registerJAWSDB_URL()) {
            log.warn("Failed to register the JAWSDB_URL environment variable macro, a macro with that name is already registered.");
        }

        if (!registerServletPort()) {
            log.warn("Failed to register the PORT environment variable macro, a macro with that name is already registered.");
        }
    }

    private static boolean registerServletPort() {
        return EnvironmentOverride.registerMacro("PORT", (environmentValue, configuration) -> {
            configuration.set("web-servlet.port", Integer.valueOf(environmentValue));
        });
    }

    private static boolean registerJAWSDB_URL() {
        return EnvironmentOverride.registerMacro("JAWSDB_URL", (environmentValue, configuration) -> {
            if (!configuration.isSet("database.type")) {
                return;
            }

            Matcher matcher = jawsDBUrlPattern.matcher(environmentValue);
            if (!matcher.matches()) {
                log.debug("Invalid JAWSDB URL environment variable was found, ignoring variable");
                log.debug("JAWSDB URL environment value: " + environmentValue);
                return;
            }

            log.debug("Valid JAWSDB URL environment variable was found, storing the variable to the config");
            log.debug("JAWSDB URL environment value: " + environmentValue);

            MatchResult result = matcher.toMatchResult();

            configuration.set("database.type", "mysql");
            configuration.set("database.username", result.group(2));
            configuration.set("database.password", result.group(3));
            configuration.set("database.hostname", result.group(4));
            configuration.set("database.database", result.group(5));
        });
    }
}
