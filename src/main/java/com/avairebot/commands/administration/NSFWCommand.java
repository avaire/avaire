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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NSFWCommand extends Command {

    public NSFWCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "NSFW Command";
    }

    @Override
    public String getDescription() {
        return "Displays the NSFW status of the current channel, additionally on/off can be passed to the command to change the channels NSFW status.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("nsfw");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the NSFW status of the current channel.",
            "`:command <on | off>` - Changes the NSFW status of the current channel.",
            "`:command <channel>` - Displays the mentioned channels NSFW status",
            "`:command <channel> <on | off>` - Changes the NSFW status of the mentioned channel."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command`",
            "`:command on`",
            "`:command #nsfw-stuff`",
            "`:command #general off`"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:all,general.manage_channels",
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendChannelStatus(context, context.getChannel());
        }

        if (args.length == 1) {
            switch (ComparatorUtil.getFuzzyType(args[0])) {
                case TRUE:
                    return updateChannelStatus(context, context.getChannel(), true);

                case FALSE:
                    return updateChannelStatus(context, context.getChannel(), false);

                case UNKNOWN:
                    Channel channel = MentionableUtil.getChannel(context.getMessage(), args);

                    if (channel != null && (channel instanceof TextChannel)) {
                        return sendChannelStatus(context, (TextChannel) channel);
                    }
                    return sendErrorMessage(context, context.i18n("invalidChannelOrStatus"));
            }
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null || !(channel instanceof TextChannel)) {
            return sendErrorMessage(context, context.i18n("notValidChannel", args[0]));
        }

        ComparatorUtil.ComparatorType fuzzyType = ComparatorUtil.getFuzzyType(args[1]);
        if (fuzzyType.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
            return sendErrorMessage(context, context.i18n("notValidStatus", args[1]));
        }

        return updateChannelStatus(context, (TextChannel) channel, fuzzyType.getValue());
    }

    private boolean updateChannelStatus(CommandMessage context, TextChannel textChannel, boolean status) {
        textChannel.getManager().setNSFW(status).queue(aVoid -> {
            context.makeSuccess(context.i18n("updated"))
                .set("textChannel", textChannel.getAsMention())
                .set("status", context.i18n("status." + (status ? "enabled" : "disabled")))
                .queue();
        }, throwable -> {
            context.makeError("Something went wrong while trying to update the channel status: " + throwable.getLocalizedMessage())
                .queue();
        });

        return true;
    }

    private boolean sendChannelStatus(CommandMessage context, TextChannel textChannel) {
        context.makeInfo(context.i18n("message"))
            .set("textChannel", textChannel.getAsMention())
            .set("status", context.i18n("status." + (textChannel.isNSFW() ? "enabled" : "disabled")))
            .queue();

        return false;
    }
}
