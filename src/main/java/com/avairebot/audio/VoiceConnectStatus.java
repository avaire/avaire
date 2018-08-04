package com.avairebot.audio;

public enum VoiceConnectStatus {
    
    /**
     * The bot was connected to the voice channel successfully.
     */
    CONNECTED(true),
    /**
     * The user that ran the music command is not in a valid voice channel.
     */
    NOT_CONNECTED(false, "You have to be connected to a voice channel."),
    /**
     * There is a user limit on the voice channel the user is currently
     * in and the bot doesn't have permission to bypass it.
     */
    USER_LIMIT(false, "Unable to connect to the voice channel you're in due to user limit!"),
    /**
     * The bot doesn't have permissions to connect to the users current voice channel.
     */
    MISSING_PERMISSIONS(false, "Unable to connect to the voice channel you're in due to missing permissions!");

    private final boolean success;
    private final String errorMessage;

    VoiceConnectStatus(boolean success) {
        this(success, null);
    }

    VoiceConnectStatus(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
