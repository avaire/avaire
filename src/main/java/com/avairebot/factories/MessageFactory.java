package com.avairebot.factories;

import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.PlaceholderType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

import java.awt.*;
import java.util.Arrays;

public class MessageFactory {

    public static PlaceholderMessage makeError(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getChannel(),
            createEmbeddedBuilder().setColor(MessageType.ERROR.getColor()),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, jdaMessage);
    }

    public static PlaceholderMessage makeWarning(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getChannel(),
            createEmbeddedBuilder().setColor(MessageType.WARNING.getColor()),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, jdaMessage);
    }

    public static PlaceholderMessage makeSuccess(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getChannel(),
            createEmbeddedBuilder().setColor(MessageType.SUCCESS.getColor()),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, jdaMessage);
    }

    public static PlaceholderMessage makeInfo(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getChannel(),
            createEmbeddedBuilder().setColor(MessageType.INFO.getColor()),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, jdaMessage);
    }

    public static PlaceholderMessage makeEmbeddedMessage(Message jdaMessage, Color color, String message) {
        return new PlaceholderMessage(jdaMessage.getChannel(),
            createEmbeddedBuilder().setColor(color),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, jdaMessage);
    }

    public static PlaceholderMessage makeEmbeddedMessage(MessageChannel channel, Color color, String message) {
        return new PlaceholderMessage(channel,
            createEmbeddedBuilder().setColor(color),
            message
        ).setGlobalPlaceholderType(PlaceholderType.ALL, channel);
    }

    public static PlaceholderMessage makeEmbeddedMessage(MessageChannel channel, MessageType type, Field... fields) {
        return makeEmbeddedMessage(channel, type.getColor(), fields);
    }

    public static PlaceholderMessage makeEmbeddedMessage(MessageChannel channel, Color color, Field... fields) {
        PlaceholderMessage message = new PlaceholderMessage(channel,
            createEmbeddedBuilder().setColor(color),
            null
        ).setGlobalPlaceholderType(PlaceholderType.ALL, channel);

        Arrays.stream(fields).forEachOrdered(message::addField);
        return message;
    }

    public static PlaceholderMessage makeEmbeddedMessage(MessageChannel channel) {
        return new PlaceholderMessage(channel, createEmbeddedBuilder(), null);
    }

    public static EmbedBuilder createEmbeddedBuilder() {
        return new EmbedBuilder();
    }
}
