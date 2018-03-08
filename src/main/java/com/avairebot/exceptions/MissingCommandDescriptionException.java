package com.avairebot.exceptions;

import com.avairebot.contracts.commands.Command;

import javax.annotation.Nonnull;

public class MissingCommandDescriptionException extends RuntimeException {

    public MissingCommandDescriptionException(@Nonnull Command command) {
        super(String.format(
            "The %s class is missing an implementation of the command description method.",
            command.getClass().getSimpleName()
        ));
    }
}
