/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.factories;

import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.PlaceholderType;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

    public static void deleteMessage(@Nonnull Message message) {
        deleteMessage(message, 0, TimeUnit.SECONDS);
    }

    public static void deleteMessage(@Nonnull Message message, int delay) {
        deleteMessage(message, delay, TimeUnit.SECONDS);
    }

    public static void deleteMessage(@Nonnull Message message, int delay, TimeUnit timeUnit) {
        if (message.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
            handleDeleteMessage(message, delay, timeUnit);
        } else if (message.getGuild() != null && message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            handleDeleteMessage(message, delay, timeUnit);
        }
    }

    private static void handleDeleteMessage(@Nonnull Message message, int delay, TimeUnit timeUnit) {
        if (delay < 1 || timeUnit == null) {
            message.delete().queue(null, RestActionUtil.ignore);
        } else {
            message.delete().queueAfter(delay, timeUnit, null, RestActionUtil.ignore);
        }
    }
}
