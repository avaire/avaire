package com.avairebot;

import com.avairebot.contracts.config.PropertyConfiguration;

public class GitInfo extends PropertyConfiguration {

    private static GitInfo instance;

    public final String branch;
    public final String commitId;
    public final String commitIdAbbrev;
    public final String commitUserName;
    public final String commitUserEmail;
    public final String commitMessageFull;
    public final String commitMessageShort;
    public final long commitTime;

    private GitInfo() {
        loadProperty(getClass().getClassLoader(), "git.properties");

        this.branch = String.valueOf(properties.getOrDefault("git.branch", ""));
        this.commitId = String.valueOf(properties.getOrDefault("git.commit.id", ""));
        this.commitIdAbbrev = String.valueOf(properties.getOrDefault("git.commit.id.abbrev", ""));
        this.commitUserName = String.valueOf(properties.getOrDefault("git.commit.user.name", ""));
        this.commitUserEmail = String.valueOf(properties.getOrDefault("git.commit.user.email", ""));
        this.commitMessageFull = String.valueOf(properties.getOrDefault("git.commit.message.full", ""));
        this.commitMessageShort = String.valueOf(properties.getOrDefault("git.commit.message.short", ""));
        this.commitTime = Long.parseLong(String.valueOf(properties.getOrDefault("git.commit.time", "0")));
    }

    public static GitInfo getGitInfo() {
        if (instance == null) {
            instance = new GitInfo();
        }
        return instance;
    }
}
