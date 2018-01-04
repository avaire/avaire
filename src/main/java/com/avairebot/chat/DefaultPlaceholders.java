package com.avairebot.chat;

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
        return message
            .replaceAll(":guildid", guild.getId());
    }

    static String toChannel(Message message, String string) {
        if (message.getTextChannel() == null || string == null) return string;
        return parseChannel(message.getTextChannel(), string);
    }

    private static String parseChannel(TextChannel channel, String message) {
        return message
            .replaceAll(":channelname", channel.getName())
            .replaceAll(":channelid", channel.getId())
            .replaceAll(":channel", channel.getAsMention());
    }

    static String toUser(Message message, String string) {
        if (message.getAuthor() == null || string == null) return string;
        return parseUser(message.getAuthor(), string);
    }

    private static String parseUser(User author, String message) {
        return message
            .replaceAll(":username", author.getName())
            .replaceAll(":userid", author.getId())
            .replaceAll(":user", author.getAsMention());
    }
}
