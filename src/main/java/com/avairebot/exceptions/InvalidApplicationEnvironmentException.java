package com.avairebot.exceptions;

public class InvalidApplicationEnvironmentException extends Exception {

    public InvalidApplicationEnvironmentException(String type) {
        super(type + " is not a valid application environment");
    }
}
