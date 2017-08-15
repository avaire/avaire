package com.avairebot.orion.factories;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;
import java.util.Arrays;

import static com.avairebot.orion.factories.MessageFactory.MessageType.*;

public class MessageFactory {

    public static RestAction<Message> makeError(Message jdaMessage, String message, Object... args) {
        return makeEmbeddedMessage(jdaMessage, ERROR, message, args);
    }

    public static RestAction<Message> makeWarning(Message jdaMessage, String message, Object... args) {
        return makeEmbeddedMessage(jdaMessage, WARNING, message, args);
    }

    public static RestAction<Message> makeSuccess(Message jdaMessage, String message, Object... args) {
        return makeEmbeddedMessage(jdaMessage, SUCCESS, message, args);
    }

    public static RestAction<Message> makeInfo(Message jdaMessage, String message, Object... args) {
        return makeEmbeddedMessage(jdaMessage, INFO, message, args);
    }

    public static RestAction<Message> makeEmbeddedMessage(Message jdaMessage, MessageType type, String message, Object... args) {
        return makeEmbeddedMessage(jdaMessage.getChannel(), type.getColor(), prepareMessage(jdaMessage, message, args));
    }

    public static RestAction<Message> makeEmbeddedMessage(MessageChannel channel, Color color, String message) {
        return channel.sendMessage(new EmbedBuilder().setColor(color).setDescription(message).build());
    }

    public static RestAction<Message> makeEmbeddedMessage(MessageChannel channel, MessageType type, Field... fields) {
        EmbedBuilder embed = new EmbedBuilder().setColor(type.getColor());
        Arrays.stream(fields).forEachOrdered(embed::addField);
        return channel.sendMessage(embed.build());
    }

    private static String prepareMessage(Message jdaMessage, String message, Object... args) {
        return String.format(message, args);
    }

    public enum MessageType {
        ERROR("#EF5350"),
        WARNING("#FAA61A"),
        SUCCESS("#43B581"),
        INFO("#3A71C1");

        private final String color;

        MessageType(String color) {
            this.color = color;
        }

        public Color getColor() {
            return Color.decode(this.color);
        }
    }
}
