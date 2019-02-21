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
import net.dv8tion.jda.core.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RepeatCommand extends Command {

    public RepeatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Repeat Command";
    }

    @Override
    public String getDescription() {
        return "I will repeat anything you say.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Repeats the given message");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command I am a BOT`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("repeat", "echo");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        if (context.mentionsEveryone() && !canMentionEveryone(context)) {
            return sendErrorMessage(context, "errors.cantMentionEveryone");
        }

        context.getMessageChannel().sendMessage(context.getContentRaw()).queue();

        return true;
    }

    private boolean canMentionEveryone(CommandMessage context) {
        return !context.isGuildMessage()
            || context.getMember().hasPermission(
            context.getChannel(), Permission.MESSAGE_MENTION_EVERYONE
        );
    }
}
