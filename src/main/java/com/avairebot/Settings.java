package com.avairebot;

import com.avairebot.utilities.NumberUtil;
import org.apache.commons.cli.CommandLine;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

public class Settings {

    private final int shardCount;
    private final boolean useColors;
    private final boolean useDebugging;
    private final boolean internalRestart;

    private final List<String> jarArgs;
    private final List<String> runtimeArgs;


    Settings(CommandLine cmd, String[] args) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
        useColors = !cmd.hasOption("no-colors");
        useDebugging = cmd.hasOption("debug");
        internalRestart = cmd.hasOption("internal-restart");

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

    public boolean useInternalRestart() {
        return internalRestart;
    }

    public List<String> getJarArgs() {
        return jarArgs;
    }

    public List<String> getRuntimeArgs() {
        return runtimeArgs;
    }
}
