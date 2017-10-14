package com.avairebot.orion.utilities;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class PlaceholderUtil {

    public static String parse(Message message, String string, PlaceholderType... types) {
        for (PlaceholderType type : types) {
            string = type.function.parse(message, string);
        }
        return string;
    }

    private static String toGuild(Message message, String string) {
        if (!message.getChannelType().isGuild()) {
            return string;
        }

        return parseGuild(message.getGuild(), string);
    }

    public static String parseGuild(Guild guild, String string) {
        string = string.replaceAll("%server%", guild.getName());
        string = string.replaceAll("%servername%", guild.getName());
        string = string.replaceAll("%serverid%", guild.getId());

        return string;
    }

    private static String toChannel(Message message, String string) {
        return parseChannel(message.getTextChannel(), string);
    }

    public static String parseChannel(Channel channel, String string) {
        string = string.replaceAll("%channel%", "<#" + channel.getId() + ">");
        string = string.replaceAll("%channelname%", channel.getName());
        string = string.replaceAll("%channelid%", channel.getId());

        return string;
    }

    private static String toUser(Message message, String string) {
        return parseUser(message.getAuthor(), string);
    }

    public static String parseUser(User user, String string) {
        string = string.replaceAll("%user%", "<@" + user.getId() + ">");
        string = string.replaceAll("%userid%", user.getId());
        string = string.replaceAll("%username%", user.getName());
        string = string.replaceAll("%userdisc%", user.getDiscriminator());

        return string;
    }

    public enum PlaceholderType {

        SERVER(PlaceholderUtil::toGuild),
        CHANNEL(PlaceholderUtil::toChannel),
        USER(PlaceholderUtil::toUser);

        private final PlaceholderFunction function;

        PlaceholderType(PlaceholderFunction function) {
            this.function = function;
        }
    }

    @FunctionalInterface
    private interface PlaceholderFunction {
        String parse(Message message, String string);
    }
}
