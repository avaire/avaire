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

import com.avairebot.contracts.config.PropertyConfiguration;

@SuppressWarnings("WeakerAccess")
public class AppInfo extends PropertyConfiguration {

    private static AppInfo instance;

    public final String version;
    public final String groupId;
    public final String artifactId;

    private AppInfo() {
        loadProperty(getClass().getClassLoader(), "app.properties");

        this.version = properties.getProperty("version");
        this.groupId = properties.getProperty("groupId");
        this.artifactId = properties.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (instance == null) {
            instance = new AppInfo();
        }
        return instance;
    }
}
