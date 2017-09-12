package com.avairebot.orion.exceptions;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

public class NoMatchFoundException extends FriendlyException {

    public NoMatchFoundException(String message, String trackUrl) {
        super(String.format(message, trackUrl), Severity.COMMON, null);
    }
}
