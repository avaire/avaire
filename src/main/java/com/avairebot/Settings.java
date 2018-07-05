package com.avairebot;

import com.avairebot.utilities.NumberUtil;
import org.apache.commons.cli.CommandLine;

public class Settings {

    private final int shardCount;
    private final boolean useColors;

    Settings(CommandLine cmd) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
        useColors = !cmd.hasOption("no-colors");
    }

    public int getShardCount() {
        return shardCount < 1 ? -1 : shardCount;
    }

    public boolean useColors() {
        return useColors;
    }
}
