package com.avairebot.orion.exceptions;

public class InvalidPluginException extends Exception {

    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
