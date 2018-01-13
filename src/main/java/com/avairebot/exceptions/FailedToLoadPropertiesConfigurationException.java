package com.avairebot.exceptions;

public class FailedToLoadPropertiesConfigurationException extends RuntimeException {

    public FailedToLoadPropertiesConfigurationException(String message, Exception exception) {
        super(message, exception);
    }
}
