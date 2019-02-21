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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SayCommand extends Command {

    public SayCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Say Command";
    }

    @Override
    public String getDescription() {
        return "The bot will repeat anything you tell it to, if a channel is mentioned, the message will be sent in that channel instead.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <message>` - Makes the bot say the given message in the current channel.",
            "`:command <channel> <message>` - Makes the bot say the given message in the channel."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command I am a BOT` - Makes the bot send the \"I am a bot\" message.",
            "`:command #general Hi there!` - Makes the bot send the message in #general."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("say");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,text.manage_messages");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        if (!context.isGuildMessage()) {
            return handleNormalCommand(context, args);
        }

        if (context.mentionsEveryone() && !canMentionEveryone(context)) {
            return sendErrorMessage(context, "errors.cantMentionEveryone");
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null || !(channel instanceof TextChannel)) {
            return handleNormalCommand(context, args);
        }

        TextChannel textChannel = (TextChannel) channel;

        if (!hasRequiredPermissionsForChannel(context.getMember(), textChannel)) {
            return sendErrorMessage(context, context.i18n("userMissingPermissions"), textChannel.getAsMention());
        }

        if (!hasRequiredPermissionsForChannel(context.getGuild().getSelfMember(), textChannel)) {
            return sendErrorMessage(context, context.i18n("botMissingPermissions"), textChannel.getAsMention());
        }

        textChannel.sendMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
            .queue(ignoredMessage -> {
                context.makeSuccess(context.i18n("success"))
                    .set("name", textChannel.getAsMention())
                    .queue(successMessage -> MessageFactory.deleteMessage(successMessage, 45, TimeUnit.SECONDS));
            });

        context.delete().reason("AvaIre say command usage").queue(null, RestActionUtil.ignore);

        return true;
    }

    private boolean handleNormalCommand(CommandMessage context, String[] args) {
        context.getMessageChannel().sendMessage(String.join(" ", args)).queue();
        context.delete().reason("AvaIre say command usage").queue(null, RestActionUtil.ignore);

        return true;
    }

    private boolean hasRequiredPermissionsForChannel(Member member, TextChannel channel) {
        return member.hasPermission(channel, Permission.MESSAGE_WRITE)
            && member.hasPermission(channel, Permission.MESSAGE_MANAGE);
    }

    private boolean canMentionEveryone(CommandMessage context) {
        return context.getMember().hasPermission(
            context.getChannel(), Permission.MESSAGE_MENTION_EVERYONE
        );
    }
}
