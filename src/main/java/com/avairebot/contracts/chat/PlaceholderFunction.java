package com.avairebot.contracts.chat;

import net.dv8tion.jda.core.entities.Message;

@FunctionalInterface
public interface PlaceholderFunction {

    /**
     * Parses the placeholder type for the given message object.
     *
     * @param message The object that should be checked to see if the placeholder can be parsed.
     * @param string  The message that should be parsed with the placeholders.
     * @return The parsed message.
     */
    String parse(Message message, String string);
}
