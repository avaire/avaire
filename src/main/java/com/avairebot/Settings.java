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

import javax.annotation.Nullable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Settings {

    private final int shardCount;
    private final int[] shards;
    private final boolean useColors;
    private final boolean useDebugging;
    private final boolean usePluginsIndex;
    private final boolean useEnvOverride;
    private final boolean musicOnlyMode;
    private final boolean generateJsonFileMode;

    private final List<String> jarArgs;
    private final List<String> runtimeArgs;

    Settings(CommandLine cmd, String[] args) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
        shards = parseShardIds(cmd);
        useColors = !cmd.hasOption("no-colors");
        useDebugging = cmd.hasOption("debug");
        usePluginsIndex = cmd.hasOption("use-plugin-index");
        useEnvOverride = cmd.hasOption("use-environment-variables");
        musicOnlyMode = cmd.hasOption("music");
        generateJsonFileMode = cmd.hasOption("generate-json-file");

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        this.runtimeArgs = runtimeMxBean.getInputArguments();
        this.jarArgs = Arrays.asList(args);
    }

    public int getShardCount() {
        return shardCount < 1 ? -1 : shardCount;
    }

    @Nullable
    public int[] getShards() {
        return shards;
    }

    public boolean useColors() {
        return useColors;
    }

    public boolean useDebugging() {
        return useDebugging;
    }

    public boolean usePluginsIndex() {
        return usePluginsIndex;
    }

    public boolean useEnvOverride() {
        return useEnvOverride;
    }

    public boolean isMusicOnlyMode() {
        return musicOnlyMode;
    }

    public boolean isGenerateJsonFileMode() {
        return generateJsonFileMode;
    }

    public List<String> getJarArgs() {
        return jarArgs;
    }

    public List<String> getRuntimeArgs() {
        return runtimeArgs;
    }

    private int[] parseShardIds(CommandLine cmd) {
        if (getShardCount() == -1 || !cmd.hasOption("shards")) {
            return null;
        }

        try {
            String[] parts = cmd.getOptionValue("shards").split("-");
            if (parts.length == 1) {
                return new int[]{
                    NumberUtil.getBetween(
                        Integer.parseInt(parts[0]), 0, getShardCount()
                    )
                };
            }

            if (parts.length != 2) {
                return null;
            }

            int min = NumberUtil.getBetween(Integer.parseInt(parts[0]), 0, getShardCount());
            int max = NumberUtil.getBetween(Integer.parseInt(parts[1]), 0, getShardCount());

            if (min == max) {
                return new int[]{min};
            }

            // If the min value is higher than the max value, we'll swap around
            // the variables so min becomes max, and max comes min.
            if (min > max) {
                max = max + min;
                min = max - min;
                max = max - min;
            }

            int range = max - min + 1;
            int[] shards = new int[range];
            for (int i = 0; i < range; i++) {
                shards[i] = min++;
            }

            return shards;
        } catch (NumberFormatException e) {
            AvaIre.getLogger().error("Failed to parse shard range for the \"--shards\" flag, error: {}", e.getMessage(), e);
            return null;
        }
    }
}
