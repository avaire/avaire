package com.avairebot;

import java.io.File;

public class Constants {

    public static final File STORAGE_PATH = new File("storage");

    // Database Tables
    public static final String GUILD_TABLE_NAME = "guilds";
    public static final String GUILD_TYPES_TABLE_NAME = "guild_types";
    public static final String STATISTICS_TABLE_NAME = "statistics";
    public static final String BLACKLIST_TABLE_NAME = "blacklists";
    public static final String PLAYER_EXPERIENCE_TABLE_NAME = "experiences";
    public static final String VOTES_TABLE_NAME = "votes";
    public static final String FEEDBACK_TABLE_NAME = "feedback";
    public static final String MUSIC_PLAYLIST_TABLE_NAME = "playlists";
    public static final String SHARDS_TABLE_NAME = "shards";
    public static final String LOG_TABLE_NAME = "logs";
    public static final String LOG_TYPES_TABLE_NAME = "log_types";

    // Package Specific Information
    public static final String PACKAGE_COMMAND_PATH = "com.avairebot.commands";
    public static final String PACKAGE_INTENTS_PATH = "com.avairebot.ai.intents";
    public static final String PACKAGE_JOB_PATH = "com.avairebot.scheduler";

    public static final String SOURCE_URI = "https://github.com/avaire/avaire/tree/master/src/main/java/com/avairebot/commands/%s/%s.java";
}
