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

package com.avairebot.utilities;

import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class StringReplacementUtil {

    /**
     * Replaces all occurrences of the given key with the given value in the given string.
     *
     * @param string The original string.
     * @param key    The key that should be removed from the original string.
     * @param value  The value that the key should be replaced with.
     * @return The string the the key replaced with the given value.
     */
    public static String replaceAll(@Nonnull String string, @Nullable String key, @Nullable String value) {
        if (key == null || value == null) {
            return string;
        }

        return string.replaceAll(
            Pattern.quote(key),
            Matcher.quoteReplacement(value)
        );
    }

    /**
     * Parses the given message using the given guild, text channel,
     * and user objects, this will use all the guild, channel,
     * and user placeholders in the given message.
     *
     * @param guild   The guild that should be used for the guild placeholders.
     * @param channel The text channel that should be used for the channel placeholders.
     * @param user    The user that should be used for the user placeholders.
     * @param message The original string that should be parsed with all the placeholders.
     * @return The parsed string after all the placeholders was replaced with the actual values.
     */
    public static String parse(@Nonnull Guild guild, @Nonnull TextChannel channel, @Nonnull User user, @Nonnull String message) {
        return StringReplacementUtil.parseChannel(channel,
            StringReplacementUtil.parseUser(user,
                StringReplacementUtil.parseGuild(guild, message)
            )
        ).replaceAll("%br%", "\n");
    }

    /**
     * Parses the given message using the given replacement
     * types and the given JDA message object.
     *
     * @param message The JDA message that should be used to get the necessary
     *                sub-objects for the given replacement types.
     * @param string  The original string that should be parsed with
     *                the given replacement types.
     * @param types   The list of types that should be used on the given string.
     * @return The parsed string after the placeholders was replaced with the actual values.
     */
    public static String parse(@Nonnull Message message, @Nonnull String string, @Nonnull ReplacementType... types) {
        for (ReplacementType type : types) {
            string = type.function.parse(message, string);
        }
        return string.replaceAll("%br%", "\n");
    }

    /**
     * Converts the message object to a guild object and parses the guild
     * and string to the {@link #parseGuild(Guild, String)} method.
     *
     * @param message The message that the guild should be retrieved from.
     * @param string  The original string that should be parsed.
     * @return The parsed string if the given message contains a valid guild instance,
     * otherwise it will just return the original unchanged string.
     */
    private static String toGuild(Message message, String string) {
        if (!message.getChannelType().isGuild()) {
            return string;
        }

        return parseGuild(message.getGuild(), string);
    }

    /**
     * Parses the given message for all the guild
     * placeholders using the given guild object.
     *
     * @param guild  The guild object that should be used to parse the placeholders.
     * @param string The original string that should be parsed.
     * @return The parsed string after all the guild placeholders was parsed.
     */
    public static String parseGuild(@Nonnull Guild guild, @Nonnull String string) {
        string = string.replaceAll("%server%", Matcher.quoteReplacement(guild.getName()));
        string = string.replaceAll("%servername%", Matcher.quoteReplacement(guild.getName()));
        string = string.replaceAll("%serverid%", guild.getId());

        return string;
    }

    /**
     * Converts the message object to a channel object and parses the channel
     * and string to the {@link #parseChannel(Channel, String)} method.
     *
     * @param message The message that the channel should be retrieved from.
     * @param string  The original string that should be parsed.
     * @return The parsed string if the given message contains a valid channel instance,
     * otherwise it will just return the original unchanged string.
     */
    private static String toChannel(@Nonnull Message message, @Nonnull String string) {
        return parseChannel(message.getTextChannel(), string);
    }

    /**
     * Parses the given message for all the channel
     * placeholders using the given channel object.
     *
     * @param channel The channel object that should be used to parse the placeholders.
     * @param string  The original string that should be parsed.
     * @return The parsed string after all the channel placeholders was parsed.
     */
    public static String parseChannel(@Nonnull Channel channel, @Nonnull String string) {
        string = string.replaceAll("%channel%", "<#" + channel.getId() + ">");
        string = string.replaceAll("%channelname%", Matcher.quoteReplacement(channel.getName()));
        string = string.replaceAll("%channelid%", channel.getId());

        return string;
    }

    /**
     * Converts the message object to a user object and parses the user
     * and string to the {@link #parseUser(User, String)} method.
     *
     * @param message The message that the user should be retrieved from.
     * @param string  The original string that should be parsed.
     * @return The parsed string using the given message author instance.
     */
    private static String toUser(@Nonnull Message message, @Nonnull String string) {
        return parseUser(message.getAuthor(), string);
    }

    /**
     * Parses the given message for all the user
     * placeholders using the given user object.
     *
     * @param user   The user object that should be used for parsing the placeholders.
     * @param string The original string that should be parsed.
     * @return The parsed string after all the user placeholders was parsed.
     */
    public static String parseUser(@Nonnull User user, @Nonnull String string) {
        string = string.replaceAll("%user%", "<@" + user.getId() + ">");
        string = string.replaceAll("%userid%", user.getId());
        string = string.replaceAll("%username%", Matcher.quoteReplacement(user.getName()));
        string = string.replaceAll("%userdisc%", user.getDiscriminator());

        return string;
    }

    /**
     * The replacement types, the replacement types serves the
     * tell the parser what methods needs to be invoked in
     * order to parse the given replacement type.
     */
    public enum ReplacementType {

        /**
         * Represent the server replacement type.
         */
        SERVER(StringReplacementUtil::toGuild),

        /**
         * Represent the channel replacement type.
         */
        CHANNEL(StringReplacementUtil::toChannel),

        /**
         * Represent the user replacement type.
         */
        USER(StringReplacementUtil::toUser);

        private final ReplacementFunction function;

        ReplacementType(ReplacementFunction function) {
            this.function = function;
        }
    }

    @FunctionalInterface
    private interface ReplacementFunction {

        /**
         * Converts the message object to the required replacement
         * type object and parses the string.
         *
         * @param message The message that the replacement type object should be retrieved from.
         * @param string  The original string that should be parsed.
         * @return The parsed string if the given message object contained the required replacement
         * type object, otherwise it will just return the original unchanged string.
         */
        String parse(Message message, String string);
    }
}
