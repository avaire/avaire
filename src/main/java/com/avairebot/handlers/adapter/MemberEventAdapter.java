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

package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.permissions.Permissions;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class MemberEventAdapter extends EventAdapter {

    private static final Logger log = LoggerFactory.getLogger(MemberEventAdapter.class);

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public MemberEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null) {
            log.warn("Failed to get a valid guild transformer during member join! User:{}, Guild:{}",
                event.getMember().getUser().getId(), event.getGuild().getId()
            );
            return;
        }

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getWelcome().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                String message = StringReplacementUtil.parse(
                    event.getGuild(), textChannel, event.getUser(),
                    channelTransformer.getWelcome().getMessage() == null ?
                        "Welcome %user% to **%server%!**" :
                        channelTransformer.getWelcome().getMessage()
                );

                String embedColor = channelTransformer.getWelcome().getEmbedColor();
                if (embedColor == null) {
                    textChannel.sendMessage(message).queue();
                    continue;
                }

                textChannel.sendMessage(
                    MessageFactory.createEmbeddedBuilder()
                        .setDescription(message)
                        .setColor(Color.decode(embedColor))
                        .build()
                ).queue();
            }
        }

        if (event.getUser().isBot()) {
            return;
        }

        if (transformer.getAutorole() != null) {
            Role role = event.getGuild().getRoleById(transformer.getAutorole());
            if (canGiveRole(event, role)) {
                event.getGuild().getController().addSingleRoleToMember(
                    event.getMember(), role
                ).queue();
            }
        }
    }

    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null) {
            log.warn("Failed to get a valid guild transformer during member leave! User:{}, Guild:{}",
                event.getMember().getUser().getId(), event.getGuild().getId()
            );
            return;
        }

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getGoodbye().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                String message = StringReplacementUtil.parse(
                    event.getGuild(), textChannel, event.getUser(),
                    channelTransformer.getGoodbye().getMessage() == null ?
                        "%user% has left **%server%**! :(" :
                        channelTransformer.getGoodbye().getMessage()
                );

                String embedColor = channelTransformer.getGoodbye().getEmbedColor();
                if (embedColor == null) {
                    textChannel.sendMessage(message).queue();
                    continue;
                }

                textChannel.sendMessage(
                    MessageFactory.createEmbeddedBuilder()
                        .setDescription(message)
                        .setColor(Color.decode(embedColor))
                        .build()
                ).queue();
            }
        }
    }

    private boolean canGiveRole(GuildMemberJoinEvent event, Role role) {
        return role != null
            && event.getGuild().getSelfMember().canInteract(role)
            && (event.getGuild().getSelfMember().hasPermission(Permissions.MANAGE_ROLES.getPermission())
            || event.getGuild().getSelfMember().hasPermission(Permissions.ADMINISTRATOR.getPermission()));
    }
}
