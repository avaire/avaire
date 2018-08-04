package com.avairebot.exceptions;

public class InvalidCommandPrefixException extends IllegalArgumentException {

    public InvalidCommandPrefixException(String message, String prefix, String newCommand, String oldCommand) {
        this(String.format(message, prefix, newCommand, oldCommand));
    }

    public InvalidCommandPrefixException(String prefix, String newCommand, String oldCommand) {
        this("The \"%s\" prefix from the \"%s\" command is already registered to the \"%s\" command!", prefix, newCommand, oldCommand);
    }

    public InvalidCommandPrefixException(String message) {
        super(message);
    }
}
