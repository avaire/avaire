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
import net.dv8tion.jda.core.entities.*;

import java.util.List;

public class MentionableUtil {

    public static User getUser(CommandMessage context, String[] args) {
        return getUser(context, args, 0);
    }

    public static User getUser(CommandMessage context, String[] args, int index) {
        if (!context.getMentionedUsers().isEmpty()) {
            return context.getMentionedUsers().get(0);
        }

        if (args.length <= index) {
            return null;
        }

        String part = args[index].trim();

        if (NumberUtil.isNumeric(part)) {
            Member member = context.getGuild().getMemberById(part);
            return member == null ? null : member.getUser();
        }

        String[] parts = part.split("#");
        if (parts.length != 2) {
            if (parts[0].trim().length() == 0) {
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

    public static Channel getChannel(Message message, String[] args) {
        return getChannel(message, args, 0);
    }

    public static Channel getChannel(Message message, String[] args, int index) {
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

        List<TextChannel> textChannelsByName = message.getGuild().getTextChannelsByName(part, true);
        if (!textChannelsByName.isEmpty()) {
            return textChannelsByName.get(0);
        }

        List<VoiceChannel> voiceChannelsByName = message.getGuild().getVoiceChannelsByName(part, true);
        if (!voiceChannelsByName.isEmpty()) {
            return voiceChannelsByName.get(0);
        }

        return null;
    }
}
