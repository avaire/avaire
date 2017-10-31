package com.avairebot.orion.factories;

import com.avairebot.orion.chat.MessageType;
import com.avairebot.orion.chat.PlaceholderMessage;
import com.avairebot.orion.chat.PlaceholderType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.Arrays;

public class MessageFactory {

    public static PlaceholderMessage makeError(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getTextChannel(),
            createEmbeddedBuilder().setColor(MessageType.ERROR.getColor()),
            PlaceholderType.ALL.parse(jdaMessage, message)
        );
    }

    public static PlaceholderMessage makeWarning(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getTextChannel(),
            createEmbeddedBuilder().setColor(MessageType.WARNING.getColor()),
            PlaceholderType.ALL.parse(jdaMessage, message)
        );
    }

    public static PlaceholderMessage makeSuccess(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getTextChannel(),
            createEmbeddedBuilder().setColor(MessageType.SUCCESS.getColor()),
            PlaceholderType.ALL.parse(jdaMessage, message)
        );
    }

    public static PlaceholderMessage makeInfo(Message jdaMessage, String message) {
        return new PlaceholderMessage(jdaMessage.getTextChannel(),
            createEmbeddedBuilder().setColor(MessageType.INFO.getColor()),
            PlaceholderType.ALL.parse(jdaMessage, message)
        );
    }

    public static EmbedBuilder makeEmbeddedMessage(Message jdaMessage, Color color, String message) {
        return createEmbeddedBuilder().setColor(color).setDescription(
            PlaceholderType.ALL.parse(jdaMessage, message)
        );
    }

    public static EmbedBuilder makeEmbeddedMessage(Guild guild, Color color, String message) {
        return createEmbeddedBuilder().setColor(color).setDescription(
            PlaceholderType.GUILD.parse(guild, message)
        );
    }

    public static EmbedBuilder makeEmbeddedMessage(MessageChannel channel, Color color, String message) {
        return createEmbeddedBuilder().setColor(color).setDescription(
            PlaceholderType.CHANNEL.parse(channel, message)
        );
    }

    public static EmbedBuilder makeEmbeddedMessage(User user, Color color, String message) {
        return createEmbeddedBuilder().setColor(color).setDescription(
            PlaceholderType.USER.parse(user, message)
        );
    }

    public static EmbedBuilder makeEmbeddedMessage(MessageType type, Field... fields) {
        return makeEmbeddedMessage(type.getColor(), fields);
    }

    public static EmbedBuilder makeEmbeddedMessage(Color color, Field... fields) {
        EmbedBuilder embed = createEmbeddedBuilder().setColor(color);
        Arrays.stream(fields).forEachOrdered(embed::addField);
        return embed;
    }

    public static EmbedBuilder createEmbeddedBuilder() {
        return new EmbedBuilder();
    }

    public static PlaceholderMessage createMessagePlaceholder(String message) {
        return new PlaceholderMessage(createEmbeddedBuilder(), message);
    }
}
