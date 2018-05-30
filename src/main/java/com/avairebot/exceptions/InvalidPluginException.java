package com.avairebot.exceptions;

public class InvalidPluginException extends Exception {

    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Object... args) {
        super(String.format(message, args));
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
