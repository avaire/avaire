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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CheckPermissionUtil {

    public static PermissionCheckType canSendMessages(CommandMessage context) {
        return canSendMessages(context.getMessageChannel());
    }

    public static PermissionCheckType canSendMessages(MessageChannel channel) {
        if (channel == null || !(channel instanceof TextChannel)) {
            return PermissionCheckType.EMBED;
        }

        TextChannel textChannel = (TextChannel) channel;
        Member member = textChannel.getGuild().getSelfMember();

        if (member.hasPermission(textChannel, Permission.ADMINISTRATOR)) {
            return PermissionCheckType.EMBED;
        }

        if (!member.hasPermission(textChannel, Permission.MESSAGE_WRITE)) {
            return PermissionCheckType.NONE;
        }

        if (!member.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS)) {
            return PermissionCheckType.MESSAGE;
        }

        if (checkForRawEmbedPermission(PermissionUtil.getExplicitPermission(
            textChannel, member
        ))) {
            return PermissionCheckType.EMBED;
        }

        if (checkForRawEmbedPermission(PermissionUtil.getExplicitPermission(
            textChannel, textChannel.getGuild().getPublicRole()
        ))) {
            return PermissionCheckType.EMBED;
        }

        if (checkForRawEmbedPermission(PermissionUtil.getExplicitPermission(
            textChannel, member.getRoles().get(0)
        ))) {
            return PermissionCheckType.EMBED;
        }

        return PermissionCheckType.MESSAGE;
    }

    private static boolean checkForRawEmbedPermission(long permissions) {
        for (Permission permission : Permission.getPermissions(permissions)) {
            if (permission.getRawValue() == 0x00004000) {
                return true;
            }
        }
        return false;
    }

    public enum PermissionCheckType {

        EMBED(true, true),
        MESSAGE(true, false),
        NONE(false, false);

        private final boolean canSendMessage;
        private final boolean canSendEmbed;

        PermissionCheckType(boolean canSendMessage, boolean canSendEmbed) {
            this.canSendMessage = canSendMessage;
            this.canSendEmbed = canSendEmbed;
        }

        public boolean canSendMessage() {
            return canSendMessage;
        }

        public boolean canSendEmbed() {
            return canSendEmbed;
        }
    }
}
