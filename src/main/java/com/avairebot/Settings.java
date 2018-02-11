package com.avairebot;

import com.avairebot.utilities.NumberUtil;
import org.apache.commons.cli.CommandLine;

public class Settings {

    private int shardCount = 0;

    Settings(CommandLine cmd) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
    }

    public int getShardCount() {
        return shardCount < 1 ? -1 : shardCount;
    }
}
