package com.avairebot.contracts.chat;

import net.dv8tion.jda.core.entities.Message;

@FunctionalInterface
public interface PlaceholderFunction {

    String parse(Message message, String string);
}
