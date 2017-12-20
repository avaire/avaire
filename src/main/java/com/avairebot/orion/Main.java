package com.avairebot.orion;

import com.avairebot.orion.utilities.NumberUtil;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
        Settings settings = new Settings();

        for (String arg : args) {
            if (arg.startsWith("-v") || arg.startsWith("--version")) {
                System.out.println(Orion.getVersionInfo());
                System.exit(0);
            }

            String[] parts = arg.split("=");
            if (parts.length != 2) {
                continue;
            }

            switch (parts[0].toLowerCase()) {
                case "-sc":
                case "--shard-count":
                    settings.shardCount = NumberUtil.parseInt(parts[1], 0);
                    break;
            }
        }

        new Orion(settings);
    }
}
