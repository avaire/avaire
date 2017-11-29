package com.avairebot.orion;

import java.io.File;

public class Constants {

    public static final File STORAGE_PATH = new File("storage");

    // Database Tables
    public static final String GUILD_TABLE_NAME = "guilds";
    public static final String GUILD_TYPES_TABLE_NAME = "guild_types";
    public static final String STATISTICS_TABLE_NAME = "statistics";
    public static final String BLACKLIST_TABLE_NAME = "blacklists";
    public static final String PLAYER_EXPERIENCE_TABLE_NAME = "experiences";
    public static final String FEEDBACK_TABLE_NAME = "feedback";
    public static final String MUSIC_PLAYLIST_TABLE_NAME = "playlists";
    public static final String SHARDS_TABLE_NAME = "shards";

    // Package Specific Information
    public static final String PACKAGE_COMMAND_PATH = "com.avairebot.orion.commands";
    public static final String PACKAGE_INTENTS_PATH = "com.avairebot.orion.ai.intents";
    public static final String PACKAGE_JOB_PATH = "com.avairebot.orion.scheduler";
}
