package com.avairebot.exceptions;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

public class TrackLoadFailedException extends FriendlyException {

    public TrackLoadFailedException(String message, String reason, Throwable cause) {
        super(String.format(message, reason), Severity.COMMON, cause);
    }
}
