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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import javax.annotation.Nullable;

public class CheckPermissionUtil {

    /**
     * Checks if the bot can send embed messages in the given message channel.
     *
     * @param channel The message channel that should be checked.
     * @return <code>True</code> if the bot can send a message in it, <code>False</code> otherwise.
     */
    public static PermissionCheckType canSendMessages(@Nullable MessageChannel channel) {
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

    /**
     * Checks if the given permission value includes the raw embed permission value.
     *
     * @param permissions The permission value that should be checked.
     * @return <code>True</code> if the given raw permission value includes
     * the embed permissions, <code>False</code> otherwise.
     */
    private static boolean checkForRawEmbedPermission(long permissions) {
        for (Permission permission : Permission.getPermissions(permissions)) {
            if (permission.getRawValue() == 0x00004000) {
                return true;
            }
        }
        return false;
    }

    /**
     * The permission check type, the permission type are used to describe
     * what type of permissions the bot has for the current channel.
     */
    public enum PermissionCheckType {

        /**
         * Represents the bot having access to both
         * send, and embed send permissions.
         */
        EMBED(true, true),

        /**
         * Represents the bot having access to send messages,
         * but not to send embed message permissions.
         */
        MESSAGE(true, false),

        /**
         * Represents the bot not having access to send messages in any form.
         */
        NONE(false, false);

        private final boolean canSendMessage;
        private final boolean canSendEmbed;

        PermissionCheckType(boolean canSendMessage, boolean canSendEmbed) {
            this.canSendMessage = canSendMessage;
            this.canSendEmbed = canSendEmbed;
        }

        /**
         * Checks if the current type allows sending normal messages.
         *
         * @return <code>True</code> if the type allows sending normal
         * messages, <code>False</code> otherwise.
         */
        public boolean canSendMessage() {
            return canSendMessage;
        }

        /**
         * Checks if the current type allows sending embed messages.
         *
         * @return <code>True</code> if the type allows sending embed
         * messages, <code>False</code> otherwise.
         */
        public boolean canSendEmbed() {
            return canSendEmbed;
        }
    }
}
