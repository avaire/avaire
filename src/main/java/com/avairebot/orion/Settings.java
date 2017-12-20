package com.avairebot.orion;

import com.avairebot.orion.utilities.NumberUtil;
import org.apache.commons.cli.CommandLine;

public class Settings {

    private int shardCount = 0;

    Settings(CommandLine cmd) {
        shardCount = NumberUtil.parseInt(cmd.getOptionValue("shard-count", "0"));
    }

    public int getShardCount() {
        return shardCount;
    }
}
