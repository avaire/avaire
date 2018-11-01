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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoCommand extends Command {

    public UserInfoCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "User Info Command";
    }

    @Override
    public String getDescription() {
        return "Shows information about the user that ran the command, or the mentioned user. This includes the users username, ID, roles, the date they joined the server, the date they created their account, and how many servers they're in (That Ava knows about).";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [user]` - Gets information about the user who ran the command, or the mentioned user");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @Senither`",
            "`:command alexis`",
            "`:command 88739639380172800`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(UserIdCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("userinfo", "uinfo");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Member member = context.getMember();
        if (args.length > 0) {
            User user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
            if (user == null) {
                return sendErrorMessage(context, "errors.noUsersWithNameOrId", args[0]);
            }
            member = context.getGuild().getMember(user);
        }

        Carbon joinedDate = Carbon.createFromOffsetDateTime(member.getJoinDate());
        Carbon createdDate = Carbon.createFromOffsetDateTime(member.getUser().getCreationTime());

        PlaceholderMessage placeholderMessage = context.makeEmbeddedMessage(getRoleColor(member.getRoles()),
            new MessageEmbed.Field(
                context.i18n("fields.username"),
                member.getUser().getName(),
                true
            ),
            new MessageEmbed.Field(
                context.i18n("fields.userId"),
                member.getUser().getId(),
                true
            ),
            new MessageEmbed.Field(
                context.i18n("fields.joinedServer"),
                joinedDate.format(context.i18n("timeFormat")) + "\n*About " + shortenDiffForHumans(joinedDate) + "*",
                true
            ),
            new MessageEmbed.Field(
                context.i18n("fields.joinedDiscord"),
                createdDate.format(context.i18n("timeFormat")) + "\n*About " + shortenDiffForHumans(createdDate) + "*",
                true
            )
        ).setThumbnail(member.getUser().getEffectiveAvatarUrl());

        String memberRoles = context.i18n("noRoles");
        if (!member.getRoles().isEmpty()) {
            memberRoles = member.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining("\n"));
        }

        placeholderMessage.addField(new MessageEmbed.Field(
            context.i18n("fields.roles", member.getRoles().size()), memberRoles, true
        ));

        placeholderMessage.addField(new MessageEmbed.Field(
            context.i18n("fields.servers"),
            context.i18n("inServers", NumberUtil.formatNicely(
                avaire.getShardManager().getMutualGuilds(member.getUser()).size()
            )), true));

        placeholderMessage.requestedBy(context.getMember()).queue();
        return true;
    }

    private String shortenDiffForHumans(Carbon carbon) {
        String diff = carbon.diffForHumans();
        if (!diff.contains("and")) {
            return diff;
        }
        return diff.split("and")[0] + "ago";
    }

    private Color getRoleColor(List<Role> roles) {
        for (Role role : roles) {
            if (role.getColor() != null) return role.getColor();
        }
        return Color.decode("#E91E63");
    }
}
