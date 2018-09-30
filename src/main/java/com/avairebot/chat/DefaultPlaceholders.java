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

package com.avairebot.chat;

import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.*;

class DefaultPlaceholders {

    static String parse(PlaceholderType type, Object object, String message) {
        switch (type) {
            case ALL:
                if (object instanceof Message && ((Message) object).getChannelType().isGuild()) {
                    Message jdaMessage = (Message) object;

                    return parseGuild(jdaMessage.getGuild(), parseChannel(jdaMessage.getTextChannel(), parseUser(jdaMessage.getAuthor(), message)));
                }

            case GUILD:
                if (object instanceof Guild) {
                    return parseGuild((Guild) object, message);
                }

                if (object instanceof Message && ((Message) object).getChannelType().isGuild()) {
                    return parseGuild(((Message) object).getGuild(), message);
                }
                break;

            case CHANNEL:
                if (object instanceof TextChannel) {
                    return parseChannel((TextChannel) object, message);
                }

                if (object instanceof Message && ((Message) object).getChannelType().equals(ChannelType.TEXT)) {
                    return parseChannel(((Message) object).getTextChannel(), message);
                }
                break;

            case USER:
                if (object instanceof User) {
                    return parseUser((User) object, message);
                }

                if (object instanceof Message && ((Message) object).getAuthor() != null) {
                    return parseUser(((Message) object).getAuthor(), message);
                }
                break;

            default:
                for (PlaceholderType placeholderType : PlaceholderType.values()) {
                    message = parse(placeholderType, object, message);
                }
                return message;
        }

        return message;
    }

    static String toGuild(Message message, String string) {
        if (!message.getChannelType().isGuild() || string == null) return string;
        return parseGuild(message.getGuild(), string);
    }

    private static String parseGuild(Guild guild, String message) {
        return StringReplacementUtil.replaceAll(message, ":guildid", guild.getId());
    }

    static String toChannel(Message message, String string) {
        if (message.getTextChannel() == null || string == null) return string;
        return parseChannel(message.getTextChannel(), string);
    }

    private static String parseChannel(TextChannel channel, String message) {
        message = StringReplacementUtil.replaceAll(message, ":channelname", channel.getName());
        message = StringReplacementUtil.replaceAll(message, ":channelid", channel.getId());
        message = StringReplacementUtil.replaceAll(message, ":channel", channel.getAsMention());

        return message;
    }

    static String toUser(Message message, String string) {
        if (message.getAuthor() == null || string == null) return string;
        return parseUser(message.getAuthor(), string);
    }

    private static String parseUser(User author, String message) {
        message = StringReplacementUtil.replaceAll(message, ":username", author.getName());
        message = StringReplacementUtil.replaceAll(message, ":userid", author.getId());
        message = StringReplacementUtil.replaceAll(message, ":user", author.getAsMention());

        return message;
    }
}
