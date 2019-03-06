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

import com.avairebot.utilities.NumberUtil;
import org.apache.commons.cli.CommandLine;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Settings {

    private final int shardCount;
    private final boolean useColors;
    private final boolean useDebugging;
    private final boolean useEnvOverride;
    private final boolean musicOnlyMode;

    private final List<String> jarArgs;
    private final List<String> runtimeArgs;


    Settings(CommandLine cmd, String[] args) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
        useColors = !cmd.hasOption("no-colors");
        useDebugging = cmd.hasOption("debug");
        useEnvOverride = cmd.hasOption("use-environment-variables");
        musicOnlyMode = cmd.hasOption("music");

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        this.runtimeArgs = runtimeMxBean.getInputArguments();
        this.jarArgs = Arrays.asList(args);
    }

    public int getShardCount() {
        return shardCount < 1 ? -1 : shardCount;
    }

    public boolean useColors() {
        return useColors;
    }

    public boolean useDebugging() {
        return useDebugging;
    }

    public boolean useEnvOverride() {
        return useEnvOverride;
    }

    public boolean isMusicOnlyMode() {
        return musicOnlyMode;
    }

    public List<String> getJarArgs() {
        return jarArgs;
    }

    public List<String> getRuntimeArgs() {
        return runtimeArgs;
    }
}
