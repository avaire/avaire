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

package com.avairebot;

public enum Environment {

    PRODUCTION("production", "prod"),
    DEVELOPMENT("development", "dev");

    private final String name;
    private final String alias;

    Environment(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public static Environment fromName(String name) {
        for (Environment environment : values()) {
            if (environment.getName().equalsIgnoreCase(name) || environment.getAlias().equalsIgnoreCase(name)) {
                return environment;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}
