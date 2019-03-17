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

import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.utilities.MentionChannelFinder;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;

public class MentionableUtil {

    /**
     * A simple regular expression used to match a string
     * to see if it matches a user mention format.
     */
    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    /**
     * The channel priorities map, containing the types of priorities that can
     * be used when looking for a channel, as well as the order the finders
     * should be looped through to find the channels in the right order.
     */
    private static final EnumMap<ChannelPriorityType, List<ChannelPriorityType>> channelPriorities = new EnumMap<>(ChannelPriorityType.class);

    static {
        for (ChannelPriorityType type : ChannelPriorityType.values()) {
            //noinspection ArraysAsListWithZeroOrOneArgument
            List<ChannelPriorityType> priorities = new ArrayList<>();
            priorities.add(type);

            for (ChannelPriorityType secondaryType : ChannelPriorityType.values()) {
                if (priorities.contains(secondaryType)) {
                    continue;
                }
                priorities.add(secondaryType);
            }
            channelPriorities.put(type, priorities);
        }
    }

    /**
     * Gets the first user object matching in the given context and arguments, the
     * method will try the following to get a user object out the other end.
     * <p>
     * <ul>
     * <li>Discord mentions (@Someone)</li>
     * <li>Full name mentions (Senither#000)</li>
     * <li>Name mentions (Senither)</li>
     * <li>User ID (88739639380172800)</li>
     * </ul>
     * <p>
     * If none of the checks finds a valid user object, <code>null</code> will be returned instead.
     *
     * @param context The command message context.
     * @param args    The arguments parsed to the command.
     * @return Possibly-null, or the user matching the first index.
     */
    @Nullable
    public static User getUser(@Nonnull CommandMessage context, @Nonnull String[] args) {
        return getUser(context, args, 0);
    }

    /**
     * Gets the <code>N</code>th index user object matching in the given context and arguments,
     * the method will try the following to get a user object out the other end.
     * <ul>
     * <li>Discord mentions (@Someone)</li>
     * <li>Full name mentions (Senither#000)</li>
     * <li>Name mentions (Senither)</li>
     * <li>User ID (88739639380172800)</li>
     * </ul>
     * <p>
     * If none of the checks finds a valid user object, <code>null</code> will be returned instead.
     *
     * @param context The command message context.
     * @param args    The arguments parsed to the command.
     * @param index   The index of the argument that should be checked.
     * @return Possibly-null, or the user matching the given index.
     */
    @Nullable
    public static User getUser(@Nonnull CommandMessage context, @Nonnull String[] args, int index) {
        if (args.length <= index) {
            return null;
        }

        String part = args[index].trim();

        if (!context.getMentionedUsers().isEmpty() && userRegEX.matcher(args[index]).matches()) {
            String userId = part.substring(2, part.length() - 1);
            if (userId.charAt(0) == '!') {
                userId = userId.substring(1, userId.length());
            }

            try {
                Member member = context.getGuild().getMemberById(userId);
                return member == null ? null : member.getUser();
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (NumberUtil.isNumeric(part)) {
            try {
                Member member = context.getGuild().getMemberById(part);
                return member == null ? null : member.getUser();
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String[] parts = part.split("#");
        if (parts.length != 2) {
            if (parts.length == 0 || parts[0].trim().length() == 0) {
                return null;
            }

            List<Member> effectiveName = context.getGuild().getMembersByEffectiveName(parts[0], true);

            if (effectiveName.isEmpty()) {
                return null;
            }
            return effectiveName.get(0).getUser();
        }

        if (parts[0].length() == 0) {
            return null;
        }

        List<Member> members = context.getGuild().getMembersByName(parts[0], true);
        for (Member member : members) {
            if (member.getUser().getDiscriminator().equals(parts[1])) {
                return member.getUser();
            }
        }

        return null;
    }

    /**
     * Gets the first channel object matching in the given context and arguments, the
     * method will try the following to get a channel object out the other end.
     * <ul>
     * <li>Discord mentions (#general)</li>
     * <li>Name mentions (general)</li>
     * <li>Channel ID (299205123673030658)</li>
     * </ul>
     * <p>
     * If none of the checks finds a valid channel object, <code>null</code> will be returned instead.
     *
     * @param message The command message.
     * @param args    The arguments parsed to the command.
     * @return Possibly-null, or the first channel matching the given arguments.
     */
    public static Channel getChannel(@Nonnull Message message, @Nonnull String[] args) {
        return getChannel(message, args, 0);
    }

    /**
     * Gets the <code>N</code>th index channel object matching in the given
     * context and arguments, the method will try the following to get
     * a channel object out the other end.
     * <ul>
     * <li>Discord mentions (#general)</li>
     * <li>Name mentions (general)</li>
     * <li>Channel ID (299205123673030658)</li>
     * </ul>
     * <p>
     * If none of the checks finds a valid channel object, <code>null</code> will be returned instead.
     *
     * @param message The command message.
     * @param args    The arguments parsed to the command.
     * @param index   The index of the argument that should be checked.
     * @return Possibly-null, or the channel matching the given index.
     */
    public static Channel getChannel(@Nonnull Message message, @Nonnull String[] args, int index) {
        return getChannel(message, args, index, ChannelPriorityType.TEXT);
    }

    /**
     * Gets the <code>N</code>th index channel object matching in the given
     * context and arguments, the method will try the following to get
     * a channel object out the other end.
     * <ul>
     * <li>Discord mentions (#general)</li>
     * <li>Name mentions (general)</li>
     * <li>Channel ID (299205123673030658)</li>
     * </ul>
     * <p>
     * If none of the checks finds a valid channel object, <code>null</code> will be returned instead.
     *
     * @param message  The command message.
     * @param args     The arguments parsed to the command.
     * @param index    The index of the argument that should be checked.
     * @param priority The type of channel to prioritise to look for.
     * @return Possibly-null, or the channel matching the given index.
     */
    public static Channel getChannel(@Nonnull Message message, @Nonnull String[] args, int index, @Nonnull ChannelPriorityType priority) {
        if (!message.getMentionedChannels().isEmpty()) {
            return message.getMentionedChannels().get(0);
        }

        if (args.length <= index) {
            return null;
        }

        String part = args[index].trim();

        if (NumberUtil.isNumeric(part)) {
            TextChannel textChannel = message.getGuild().getTextChannelById(part);
            if (textChannel != null) {
                return textChannel;
            }
            return message.getGuild().getVoiceChannelById(part);
        }

        for (ChannelPriorityType type : channelPriorities.get(priority)) {
            Channel channel = type.find(message, part);
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    /**
     * The channel priority type, used for specifying what type of channel
     * should be prioritised when looking for a channel by name.
     */
    public enum ChannelPriorityType {
        /**
         * Text channel finder, looks for a {@link TextChannel text channel}
         * in the given message with the given name.
         */
        TEXT(((message, name) -> {
            List<TextChannel> channels = message.getGuild().getTextChannelsByName(name, true);
            return channels.isEmpty() ? null : channels.get(0);
        })),

        /**
         * Voice channel finder, looks for a {@link VoiceChannel voice channel}
         * in the given message with the given name.
         */
        VOICE(((message, name) -> {
            List<VoiceChannel> channels = message.getGuild().getVoiceChannelsByName(name, true);
            return channels.isEmpty() ? null : channels.get(0);
        }));

        private final MentionChannelFinder finder;

        ChannelPriorityType(MentionChannelFinder getter) {
            this.finder = getter;
        }

        /**
         * Tries to find the a channel using the given JDA message object and
         * name, if no channel were found then {@code NULL} will be returned.
         *
         * @param message The JDA message object instance.
         * @param name    The name of the channel to look for.
         * @return Possibly-null, the first channel with the given name.
         */
        public Channel find(@Nonnull Message message, @Nonnull String name) {
            return finder.find(message, name);
        }
    }
}
